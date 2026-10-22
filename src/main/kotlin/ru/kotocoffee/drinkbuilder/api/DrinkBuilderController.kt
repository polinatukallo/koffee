package ru.kotocoffee.drinkbuilder.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kotocoffee.drinkbuilder.application.DrinkBuilderService

@RestController
@RequestMapping("/api/drinks")
@Tag(name = "Конструктор напитков", description = "Расчёт цены и состава до оформления заказа")
class DrinkBuilderController(private val drinkBuilderService: DrinkBuilderService) {

    @PostMapping("/preview")
    @Operation(
        summary = "Собрать напиток и рассчитать цену",
        description = "Проверяет совместимость и лимиты (422 с полем violations при нарушении), " +
            "считает цену, объём и состав.",
    )
    fun preview(@Valid @RequestBody request: DrinkConfigurationRequest): DrinkPreviewResponse =
        DrinkPreviewResponse.from(drinkBuilderService.preview(request.toConfiguration()))
}
