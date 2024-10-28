package com.techatpark.workout.component.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.gurukulams.questionbank.model.QuestionChoice;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.util.UUID;

@JsonComponent
public class QuestionChoiceConverter {
    public static class Serialize extends JsonSerializer<QuestionChoice> {

        /**
         * Serialize to QuestionChoice.
         *
         * @param questionChoice
         * @param jsonGenerator
         * @param serializerProvider
         * @throws IOException
         */
        @Override
        public void serialize(final QuestionChoice questionChoice,
                              final JsonGenerator jsonGenerator,
                              final SerializerProvider serializerProvider)
                throws IOException {
            if (questionChoice == null) {
                jsonGenerator.writeNull();
            } else {
                jsonGenerator.writeStartObject();
                if (questionChoice.id() != null) {
                    jsonGenerator.writeStringField("id",
                            questionChoice.id().toString());
                }
                jsonGenerator.writeStringField("value",
                        questionChoice.cValue());
                if (questionChoice.isAnswer() != null) {
                    jsonGenerator.writeBooleanField("answer",
                            questionChoice.isAnswer());
                }
                jsonGenerator.writeEndObject();
            }
        }
    }

    public static class Deserialize extends JsonDeserializer<QuestionChoice> {

        /**
         * Deserialize QuestionChoice.
         *
         * @param jsonParser
         * @param deserializationContext
         * @return
         * @throws IOException
         */
        @Override
        public QuestionChoice deserialize(final JsonParser jsonParser,
                          final DeserializationContext deserializationContext)
                throws IOException {
            JsonNode theValue = jsonParser.readValueAsTree();
            if (theValue == null) {
                return null;
            } else {
                QuestionChoice questionChoice
                        = new QuestionChoice(null, null, null, null);
                JsonNode jsonNode = theValue.path("id");
                if (jsonNode != null && jsonNode.textValue() != null) {
                    questionChoice = questionChoice
                            .withId(UUID.fromString(jsonNode.textValue()));
                }
                jsonNode = theValue.path("value");
                if (jsonNode != null) {
                    questionChoice = questionChoice
                            .withCValue(jsonNode.textValue());
                }
                jsonNode = theValue.path("answer");
                if (jsonNode != null) {
                    questionChoice = questionChoice
                            .withIsAnswer(jsonNode.booleanValue());
                }

                return questionChoice;
            }

        }
    }
}
