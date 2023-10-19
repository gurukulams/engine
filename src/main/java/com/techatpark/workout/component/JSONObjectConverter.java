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

        @Override
        public void serialize(JSONObject jsonObject,
                              JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider)
                throws IOException {
            try {
                if (jsonObject == null) {
                    jsonGenerator.writeNull();
                }
                else {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode actualObj = mapper.readTree(jsonObject.toString());
                    jsonGenerator.writeString(mapper.writeValueAsString(actualObj));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static class Deserialize extends JsonDeserializer<JSONObject> {

        @Override
        public JSONObject deserialize(JsonParser jsonParser,
                                      DeserializationContext deserializationContext)
                throws IOException, JacksonException {
            try {
                String dateAsString = jsonParser.readValueAsTree().toString();
                if (dateAsString==null) {
                    return null;
                } else {
                    return new JSONObject(dateAsString);
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
