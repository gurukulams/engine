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

import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.techatpark.workout.service.QuestionService.OWNER_USER;

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
        Question newMCQ = newMCQ();

        // No Answer
        Assertions.assertThrows(ConstraintViolationException.class, () ->
                questionService.create(List.of("c1",
                                "c2"),
                        null,
                        QuestionType.CHOOSE_THE_BEST,
                        null,
                        OWNER_USER,
                        newMCQ())
                );

        newMCQ.getChoices().remove(0);
        newMCQ.getChoices().remove(0);
        newMCQ.getChoices().remove(0);

        // No Answer
        Assertions.assertThrows(ConstraintViolationException.class, () ->
                questionService.create(List.of("c1",
                                "c2"),
                        null,
                        QuestionType.CHOOSE_THE_BEST,
                        null,
                        OWNER_USER,
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
                OWNER_USER,
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


        testUpdates(question);

    }

    private void testUpdates(final Optional<Question> question) throws SQLException {
        Question questionToUpdate = question.get();

        questionToUpdate.setQuestion("Updated");

        this.questionService.update(questionToUpdate.getType(),
                questionToUpdate.getId(),null, questionToUpdate);

        Assertions.assertEquals("Updated",
                this.questionService.read(questionToUpdate.getId(),null)
                        .get().getQuestion());

        QuestionChoice questionChoice = question.get().getChoices().get(0);

        questionChoice.setCValue("Updated");

        this.questionService.update(questionToUpdate.getType(),
                questionToUpdate.getId(),null, questionToUpdate);

        Assertions.assertEquals("Updated",
                this.questionService.read(questionToUpdate.getId(),null).get()
                    .getChoices().stream()
                    .filter(questionChoice1 -> questionChoice1.getId().equals(questionChoice.getId()))
                    .findFirst().get().getCValue());

        int existingQuestions = question.get().getChoices().size();

        String cValue = UUID.randomUUID().toString();
        QuestionChoice choice = new QuestionChoice();
        choice.setIsAnswer(Boolean.FALSE);
        choice.setCValue(cValue);
        questionToUpdate.getChoices().add(choice);

        this.questionService.update(questionToUpdate.getType(),
                questionToUpdate.getId(),null, questionToUpdate);

        QuestionChoice choiceReturned = this.questionService.read(questionToUpdate.getId(),null).get()
                .getChoices().stream()
                .filter(questionChoice1 -> questionChoice1.getCValue().equals(cValue))
                        .findFirst().get();

        Assertions.assertTrue(
                choiceReturned.getCValue().equals(cValue));

        questionToUpdate.setChoices(this.questionService.read(questionToUpdate.getId(),null)
                .get()
                .getChoices().stream()
                .filter(questionChoice1 -> !questionChoice1.getCValue().equals(cValue)).toList());

        this.questionService.update(questionToUpdate.getType(),
                questionToUpdate.getId(),null, questionToUpdate);

        Assertions.assertEquals(existingQuestions,
                this.questionService.read(questionToUpdate.getId(),null).get()
                        .getChoices().size());


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
                OWNER_USER,
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

        testUpdates(question);

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
                OWNER_USER,
                newMCQ);


        questionService.delete(question.get().getId(), QuestionType.CHOOSE_THE_BEST);

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
                OWNER_USER,
                newMCQ);

        questionService.create(List.of("c1",
                        "c2"),
                null,
                QuestionType.CHOOSE_THE_BEST,
                Locale.FRENCH,
                OWNER_USER,
                newMCQ);

        Assertions.assertEquals(2,
                questionService.list(OWNER_USER, null, List.of("c1",
                "c2")).size());

        Assertions.assertEquals(2,
                questionService.list(OWNER_USER, Locale.FRENCH, List.of("c1",
                        "c2")).size());

        Assertions.assertEquals(2,
                questionService.list("NEW_USER", null, List.of("c1",
                        "c2")).size());

        Assertions.assertEquals(2,
                questionService.list("NEW_USER", Locale.FRENCH, List.of("c1",
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