package com.techatpark.workout.service;

import com.gurukulams.core.model.QuestionChoice;
import com.gurukulams.core.service.CategoryService;
import com.techatpark.workout.payload.Question;
import com.techatpark.workout.payload.QuestionType;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@SpringBootTest
class QuestionServiceTest {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private CategoryService categoryService;

    /**
     * Before.
     *
     * @throws IOException the io exception
     */
    @BeforeEach
    void before() throws IOException, SQLException {
        cleanUp();
    }

    /**
     * After.
     */
    @AfterEach
    void after() throws SQLException {
        cleanUp();
    }

    private void cleanUp() throws SQLException {
        questionService.delete();
    }

    @Test
    void testInvalidQuestion() {
        Assertions.assertThrows(ConstraintViolationException.class, () ->
                questionService.create(List.of("c1",
                                "c2"),
                        null,
                        QuestionType.CHOOSE_THE_BEST,
                        null,
                        "sathish",
                        newMCQ())
                );
    }

    @Test
    void testChooseTheBest() throws SQLException {

        Question newMCQ = newMCQ();

        newMCQ.getChoices().get(0).setIsAnswer(true);

        // Create a Question
        Optional<Question> question = questionService.create(List.of("c1",
                        "c2"),
                null,
                QuestionType.CHOOSE_THE_BEST,
                null,
                "sathish",
                newMCQ);

        // Right Answer
        Assertions.assertTrue(answerService.answer(question.get().getId(),
                question.get().getChoices().stream()
                        .filter(QuestionChoice::getIsAnswer)
                        .findFirst().get().getId().toString()));

        // Wrong Answer
        Assertions.assertFalse(answerService.answer(question.get().getId(),
                question.get().getChoices().stream()
                        .filter(choice -> !choice.getIsAnswer())
                        .findFirst().get().getId().toString()));


    }

    @Test
    void testMultiChoice() throws SQLException {

        Question newMCQ = newMCQ();

        newMCQ.getChoices().get(0).setIsAnswer(true);
        newMCQ.getChoices().get(2).setIsAnswer(true);

        // Create a Question
        Optional<Question> question = questionService.create(List.of("c1",
                        "c2"),
                null,
                QuestionType.MULTI_CHOICE,
                null,
                "sathish",
                newMCQ);

        String rightAnswer = question.get().getChoices().stream()
                .filter(QuestionChoice::getIsAnswer)
                .map(choice -> choice.getId().toString())
                .collect(Collectors.joining(","));

        // Right Answer
        Assertions.assertTrue(answerService.answer(question.get().getId(),
                rightAnswer));

        // Wrong Answer
        Assertions.assertFalse(answerService.answer(question.get().getId(),
                rightAnswer+ "," + question.get().getChoices().stream()
                        .filter(choice -> !choice.getIsAnswer())
                        .findFirst().get().getId()));

        // Wrong Answer
        Assertions.assertFalse(answerService.answer(question.get().getId(),
                question.get().getChoices().stream()
                        .filter(choice -> !choice.getIsAnswer())
                        .findFirst().get().getId().toString()));


    }

    @Test
    void testDelete() throws SQLException {
        Question newMCQ = newMCQ();

        newMCQ.getChoices().get(0).setIsAnswer(true);

        // Create a Question
        Optional<Question> question = questionService.create(List.of("c1",
                        "c2"),
                null,
                QuestionType.CHOOSE_THE_BEST,
                null,
                "sathish",
                newMCQ);


        questionService.deleteAQuestion(question.get().getId(), QuestionType.CHOOSE_THE_BEST);

        Assertions.assertTrue(questionService.read(question.get().getId(), null).isEmpty());

    }

    @Test
    void testList() throws SQLException {
        Question newMCQ = newMCQ();

        newMCQ.getChoices().get(0).setIsAnswer(true);

        // Create a Question
        questionService.create(List.of("c1",
                        "c2"),
                null,
                QuestionType.CHOOSE_THE_BEST,
                null,
                "sathish",
                newMCQ);

        questionService.create(List.of("c1",
                        "c2"),
                null,
                QuestionType.CHOOSE_THE_BEST,
                Locale.FRENCH,
                "sathish",
                newMCQ);

        Assertions.assertEquals(2,
                questionService.list("mani", null, List.of("c1",
                "c2")).size());

        Assertions.assertEquals(2,
                questionService.list("mani", Locale.FRENCH, List.of("c1",
                        "c2")).size());

    }

    Question newMCQ() {
        Question question = new Question();
        question.setQuestion("Choose 1");
        question.setExplanation("A Choose the best question");
        question.setChoices(new ArrayList<>());

        QuestionChoice choice = new QuestionChoice();
        choice.setCValue("1");
        question.getChoices().add(choice);

        choice = new QuestionChoice();
        choice.setCValue("2");
        question.getChoices().add(choice);

        choice = new QuestionChoice();
        choice.setCValue("3");
        question.getChoices().add(choice);

        choice = new QuestionChoice();
        choice.setCValue("4");
        question.getChoices().add(choice);

        return question;
    }
}