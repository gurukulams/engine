package com.gurukulams.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gurukulams.core.model.Category;
import com.gurukulams.questionbank.model.QuestionChoice;
import com.gurukulams.questionbank.payload.Question;
import com.gurukulams.questionbank.payload.QuestionType;
import com.gurukulams.core.service.CategoryService;
import com.gurukulams.questionbank.service.QuestionService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

public class QuestionsLoader {

    /**
     * Logger.
     */
    private final Logger logger =
            LoggerFactory.getLogger(QuestionsLoader.class);

    /**
     * Quetion Owner.
     */
    public static final String USER_NAME = "tom@email.com";

    /**
     * Category Service.
     */
    private final CategoryService categoryService;

    /**
     * Json Mapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * Seed Folder.
     */
    private final String seedFolder;

    /**
     * Question Service.
     */
    private final QuestionService questionService;

    /**
     * QuestionsLoader.
     * @param theCategoryService
     * @param theObjectMapper
     * @param theSeedFolder
     * @param theQuestionService
     */
    public QuestionsLoader(final CategoryService theCategoryService,
                           final ObjectMapper theObjectMapper,
                           final String theSeedFolder,
                           final QuestionService theQuestionService) {
        this.categoryService = theCategoryService;
        this.objectMapper = theObjectMapper;
        this.seedFolder = theSeedFolder;
        this.questionService = theQuestionService;
    }

    /**
     * Loads Questions.
     */
    @PostConstruct
    public void load() throws IOException, SQLException {
        if (seedFolder != null) {
            questionService.delete();
            createAllCategory(USER_NAME);
            File questionsFolder = new File(seedFolder, "questions");
            questionsFolder.mkdirs();
            for (Path path:Files.find(Path.of(questionsFolder.getPath()),
                            Integer.MAX_VALUE,
                            (filePath, fileAttr)
                                -> fileAttr.isRegularFile()
                                && !filePath.toFile().getName().contains("-"))
                    .toList()) {
                try {
                    createQuestion(USER_NAME, path.toFile());
                } catch (Exception e) {
                    logger.error("Question can not be created for " + path, e);
                }
            }

        }
    }

    private void createAllCategory(final String userName)
            throws IOException, SQLException {
        if (seedFolder != null) {
            questionService.delete();
            File questionsFolder = new File(seedFolder, "questions");
            Files.find(Path.of(questionsFolder.getPath()),
                            Integer.MAX_VALUE,
                            (filePath, fileAttr)
                                    -> fileAttr.isDirectory())
                    .forEach(categoriesFolder -> {
                        if (!categoriesFolder.equals(questionsFolder)) {
                            String categoryName = categoriesFolder
                                    .getFileName().toString();
                            Category categories = new Category(
                                    categoryName, categoryName,
                                    categoryName, null,
                                    null, null, null);

                            try {
                                categoryService.create(userName, null,
                                        categories);
                            } catch (SQLException e) {
                                logger.debug("Category {} exists",
                                        categoryName);
                            }
                        }
                    });
        }
    }

    private Question createQuestion(final String userName,
                                    final File questionFile)
            throws SQLException {
        Question question = getObject(questionFile, Question.class);
        final String nameOfQuestion = questionFile.getName()
                .replaceFirst(".json", "");
        boolean isWindows = System
                .getProperty("os.name").toLowerCase().contains("win");
        String regexForQuestions = isWindows
                ? "\\\\questions\\\\" : "/questions/";
        String regexPath = isWindows
                ? "\\\\" : "/";
        String thePath = questionFile.getPath().split(regexForQuestions)[1];

        List<String> tokens =
                new ArrayList<>(List.of(thePath.split(regexPath)));

        tokens.remove(tokens.size() - 1);

        QuestionType questionType = null;

        if (question.getMatches() != null)  {
            questionType = QuestionType.MATCH_THE_FOLLOWING;
        } else {
            Stream<QuestionChoice> rightAnswers = question.getChoices()
                    .stream()
                    .filter(choice
                            -> choice.answer() != null
                            && choice.answer());

            questionType = rightAnswers.count() == 1
                    ? QuestionType.CHOOSE_THE_BEST
                    : QuestionType.MULTI_CHOICE;
        }

        Question createdQuestion = questionService.create(tokens,
                    null,
                    questionType,
                    null, userName, question).get();

        List<File> questionLocalizedFiles = List.of(
                Objects.requireNonNull(questionFile.getParentFile()
                        .listFiles((dir, name) -> name.endsWith(".json")
                                && name.contains(nameOfQuestion + "-"))));

        for (File questionLocalizedFile : questionLocalizedFiles) {
            Locale locale = Locale.of(questionLocalizedFile.getName()
                    .replaceFirst(nameOfQuestion + "-", "")
                    .replaceFirst(".json", ""));
            final Question questionLocalized =
                    getObject(questionLocalizedFile, Question.class);
            questionLocalized.setId(createdQuestion.getId());
            for (int i = 0; i < createdQuestion.getChoices().size(); i++) {
                QuestionChoice questionChoice = questionLocalized
                        .getChoices().get(i);

                QuestionChoice questionChoice1 = new QuestionChoice(
                        createdQuestion.getChoices().get(i).id(),
                        questionChoice.questionId(),
                        questionChoice.label(),
                        questionChoice.answer());

                questionLocalized.getChoices().set(i, questionChoice1);
            }

            questionService.update(
                    questionType,
                    createdQuestion.getId(), locale, questionLocalized).get();

        }

        return createdQuestion;
    }


    private <T> T getObject(final File jsonFile, final Class<T> type) {
        T t;
        try {
            t = objectMapper.readValue(jsonFile, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return t;
    }


}
