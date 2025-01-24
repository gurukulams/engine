package com.gurukulams.starter.openapi;


import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    /**
     * Open API Configuration.
     * @return OpenAPI
     */
    @Bean
    public OpenAPI apiDocConfig() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gurukulams API")
                        .description("API for Gurukulams learning platform")
                        .version("0.0.1")
                        .contact(new Contact()
                                .name("Gurukulams")
                                .url("www.gurukulams.com")))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentation")
                        .url("https:/wiki...."));
    }
}
