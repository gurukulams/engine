package com.techatpark.workout.starter.swagger;


import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    /**
     * Open API Configuration.
     * @return OpenAPI
     */
    @Bean
    public OpenAPI apiDocConfig() {
        return new OpenAPI()
                .info(new Info()
                        .title("Communities API")
                        .description("API for building communities and events")
                        .version("0.0.1")
                        .contact(new Contact()
                                .name("Tech@Park")
                                .email("exaample@z.com")))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentation")
                        .url("https:/wiki...."));
    }
}
