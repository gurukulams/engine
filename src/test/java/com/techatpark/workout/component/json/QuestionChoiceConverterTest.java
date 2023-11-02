package com.techatpark.workout.component.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gurukulams.questionbank.model.QuestionChoice;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class QuestionChoiceConverterTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void test() throws JsonProcessingException {


        QuestionChoice questionChoice = objectMapper.readValue(questionChoice(), QuestionChoice.class);

        Assertions.assertEquals("A",
                questionChoice.getCValue());
        Assertions.assertTrue(
                questionChoice.getIsAnswer());

    }

    private String questionChoice() {
        return """
            {
                 "value" : "A",
                 "answer" : true
               }
                """;
    }
}