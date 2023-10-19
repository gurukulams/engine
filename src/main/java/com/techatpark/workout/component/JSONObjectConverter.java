package com.techatpark.workout.component;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.json.JSONObject;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class JSONObjectConverter {
    public static class Serialize extends JsonSerializer<JSONObject> {

        /**
         * Serialize to JSONObject.
         * @param jsonObject
         * @param jsonGenerator
         * @param serializerProvider
         * @throws IOException
         */
        @Override
        public void serialize(final JSONObject jsonObject,
                  final JsonGenerator jsonGenerator,
                  final SerializerProvider serializerProvider)
                throws IOException {
            try {
                if (jsonObject == null) {
                    jsonGenerator.writeNull();
                } else {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode actualObj = mapper
                            .readTree(jsonObject.toString());
                    jsonGenerator.writeString(mapper
                            .writeValueAsString(actualObj));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class Deserialize extends JsonDeserializer<JSONObject> {

        /**
         * Deserialize JSONObject.
         * @param jsonParser
         * @param deserializationContext
         * @return
         * @throws IOException
         * @throws JacksonException
         */
        @Override
        public JSONObject deserialize(final JsonParser jsonParser,
                      final DeserializationContext deserializationContext)
                throws IOException, JacksonException {
            try {
                String dateAsString = jsonParser.getText();
                if (dateAsString == null) {
                    return null;
                } else {
                    return new JSONObject(dateAsString);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
