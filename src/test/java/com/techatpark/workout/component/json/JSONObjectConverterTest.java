package com.techatpark.workout.component.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JSONObjectConverterTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void test() throws JsonProcessingException {
        String jsonText = objectMapper.writeValueAsString(jsonObject());

        JSONObject jsonObject = objectMapper.readValue(jsonText, JSONObject.class);

        Assertions.assertEquals("http://www.w3.org/ns/anno.jsonld",
                jsonObject.get("@context"));

    }

    private JSONObject jsonObject() {
        return new JSONObject("""
                {
                		"@context": "http://www.w3.org/ns/anno.jsonld",
                		"body": [
                			{
                				"purpose": "tagging",
                				"type": "TextualBody",
                				"value": "43434"
                			}
                		],
                		"id": "#4c60efd1-78bd-4c51-b532-3f4b53c10ae1",
                		"target": {
                			"selector": [
                				{
                					"exact": "Reproduction also plays",
                					"type": "TextQuoteSelector"
                				},
                				{
                					"end": 1079,
                					"start": 1056,
                					"type": "TextPositionSelector"
                				}
                			]
                		},
                		"type": "Annotation"
                }
                """);
    }
}