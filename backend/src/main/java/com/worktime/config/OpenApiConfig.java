package com.worktime.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI workTimeOpenAPI() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title("WorkTime API")
                                .version("1.0")
                                .description("REST API for the WorkTime project management system.")
                                .contact(
                                        new Contact()
                                                .name("Ahmet Buğra Keskin")
                                                .email("abugrakskn52@gmail.com")
                                )
                );
    }
}