package com.techatpark.workout.service;

import com.techatpark.workout.model.Choice;
import com.techatpark.workout.model.Question;
import com.techatpark.workout.model.QuestionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class QuestionServiceTest {

    @Autowired
    private QuestionService questionService;

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
    void create() {
        questionService.create(List.of("c1","c2"),
                null,
                QuestionType.CHOOSE_THE_BEST,
                null,
                "sathish",
                newMCQ());
    }

    Question newMCQ() {
        Question question = new Question();
        question.setQuestion("Choose 1");
        question.setType(QuestionType.CHOOSE_THE_BEST);
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