package com.rubenlazaro.reservas.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Title and description shown at the top of the generated OpenAPI document and Swagger UI. */
@Configuration(proxyBeanMethods = false)
class OpenApiConfiguration {

    @Bean
    OpenAPI reservasOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Reservas API")
                .version("0.0.1")
                .description("Booking of resources (courts, rooms). Errors are returned as "
                        + "RFC 9457 problem details (application/problem+json)."));
    }
}
