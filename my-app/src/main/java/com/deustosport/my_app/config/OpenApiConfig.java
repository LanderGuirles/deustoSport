package com.deustosport.my_app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("DeustoSport API")
                .version("1.0.0")
                .description("API REST para la gestión de reservas y partidas de padel en DeustoSport")
                .contact(new Contact()
                    .name("DeustoSport Team")
                    .url("https://deustosport.com")
                    .email("info@deustosport.com")));
    }
}
