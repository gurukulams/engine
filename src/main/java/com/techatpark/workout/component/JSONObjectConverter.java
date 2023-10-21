package com.techatpark.workout.component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.json.JSONObject;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@JsonComponent
public class JSONObjectConverter {
    public static class Serialize extends JsonSerializer<JSONObject> {

        /**
         * Serialize to JSONObject.
         *
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
            if (jsonObject == null) {
                jsonGenerator.writeNull();
            } else {
                jsonGenerator.writeObject(jsonObject.toString());
            }
        }
    }

    public static class Deserialize extends JsonDeserializer<JSONObject> {

        /**
         * Deserialize JSONObject.
         *
         * @param jsonParser
         * @param deserializationContext
         * @return
         * @throws IOException
         */
        @Override
        public JSONObject deserialize(final JsonParser jsonParser,
                      final DeserializationContext deserializationContext)
                throws IOException {
            TypeReference<HashMap<String, Object>> typeRef =
                    new TypeReference<>() { };
            Map<String, Object> theValue = jsonParser.readValueAs(typeRef);
            if (theValue == null) {
                return null;
            } else {
                return new JSONObject(theValue);
            }
        }
    }
}
