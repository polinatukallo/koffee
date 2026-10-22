package ru.kotocoffee.shared.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun kotoCoffeeOpenApi(): OpenAPI {
        val info = Info()
            .title("KotoCoffee API")
            .version("v1")
            .description("Конструктор напитков и заказы кофейни. Все денежные суммы указаны в рублях.")
        return OpenAPI().info(info)
    }
}
