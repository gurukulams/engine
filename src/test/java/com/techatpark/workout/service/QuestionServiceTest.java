package com.techatpark.workout.service;

import com.techatpark.workout.model.Choice;
import com.techatpark.workout.model.Question;
import com.techatpark.workout.model.QuestionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
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
    void before() throws IOException {
        cleanUp();
    }

    /**
     * After.
     */
    @AfterEach
    void after() {
        cleanUp();
    }

    private void cleanUp() {
        questionService.deleteAll();
    }

    @Test
    void testChooseTheBest() {

        Question newMCQ = newMCQ();

        newMCQ.getChoices().get(0).setAnswer(true);

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
                        .filter(Choice::isAnswer)
                        .findFirst().get().getId().toString()));

        // Wrong Answer
        Assertions.assertFalse(answerService.answer(question.get().getId(),
                question.get().getChoices().stream()
                        .filter(choice -> !choice.isAnswer())
                        .findFirst().get().getId().toString()));


    }

    @Test
    void testMultiChoice() {

        Question newMCQ = newMCQ();

        newMCQ.getChoices().get(0).setAnswer(true);
        newMCQ.getChoices().get(2).setAnswer(true);

        // Create a Question
        Optional<Question> question = questionService.create(List.of("c1",
                        "c2"),
                null,
                QuestionType.MULTI_CHOICE,
                null,
                "sathish",
                newMCQ);

        // Right Answer
        Assertions.assertTrue(answerService.answer(question.get().getId(),
                question.get().getChoices().stream()
                        .filter(Choice::isAnswer)
                        .map(choice -> choice.getId().toString())
                        .collect(Collectors.joining(","))));

        // Wrong Answer
        Assertions.assertFalse(answerService.answer(question.get().getId(),
                question.get().getChoices().stream()
                        .filter(choice -> !choice.isAnswer())
                        .findFirst().get().getId().toString()));


    }

    @Test
    void testDelete() {
        Question newMCQ = newMCQ();

        newMCQ.getChoices().get(0).setAnswer(true);

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
    void testList() {
        Question newMCQ = newMCQ();

        newMCQ.getChoices().get(0).setAnswer(true);

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

        Choice choice = new Choice();
        choice.setValue("1");
        question.getChoices().add(choice);

        choice = new Choice();
        choice.setValue("2");
        question.getChoices().add(choice);

        choice = new Choice();
        choice.setValue("3");
        question.getChoices().add(choice);

        choice = new Choice();
        choice.setValue("4");
        question.getChoices().add(choice);

        return question;
    }
}