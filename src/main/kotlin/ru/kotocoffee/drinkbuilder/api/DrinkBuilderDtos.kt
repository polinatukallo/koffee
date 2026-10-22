package ru.kotocoffee.drinkbuilder.api

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import ru.kotocoffee.drinkbuilder.application.DrinkPreview
import ru.kotocoffee.drinkbuilder.domain.DrinkConfiguration
import ru.kotocoffee.drinkbuilder.domain.ModifierChoice
import ru.kotocoffee.shared.kernel.DrinkSize
import ru.kotocoffee.shared.kernel.MeasureUnit
import ru.kotocoffee.shared.kernel.ModifierCategory
import ru.kotocoffee.shared.kernel.Temperature
import ru.kotocoffee.shared.web.RequestFields
import java.math.BigDecimal

@Schema(description = "Сборка напитка")
data class DrinkConfigurationRequest(
    @field:NotNull
    @field:Positive
    @field:Schema(description = "Идентификатор базового напитка", example = "3")
    val drinkId: Long?,
    @field:NotNull
    @field:Schema(description = "Размер", example = "L")
    val size: DrinkSize?,
    @field:NotNull
    @field:Schema(description = "Температура", example = "ICED")
    val temperature: Temperature?,
    @field:Valid
    @field:Size(max = 20)
    @field:Schema(description = "Выбранные модификаторы. Если молоко не указано, подставляется молоко по умолчанию")
    val modifiers: List<ModifierChoiceRequest?> = emptyList(),
) {
    fun toConfiguration(): DrinkConfiguration =
        DrinkConfiguration(
            drinkId = RequestFields.required(drinkId, "drinkId"),
            size = RequestFields.required(size, "size"),
            temperature = RequestFields.required(temperature, "temperature"),
            modifiers = RequestFields.requiredElements(modifiers, "modifiers").map { it.toChoice() },
        )
}

@Schema(description = "Выбранный модификатор")
data class ModifierChoiceRequest(
    @field:NotNull
    @field:Positive
    @field:Schema(description = "Идентификатор модификатора", example = "2")
    val modifierId: Long?,
    @field:Min(1)
    @field:Max(10)
    @field:Schema(description = "Количество порций", example = "1", defaultValue = "1")
    val quantity: Int = 1,
) {
    fun toChoice(): ModifierChoice = ModifierChoice(RequestFields.required(modifierId, "modifierId"), quantity)
}

@Schema(description = "Предпросмотр напитка: цена, объём и состав")
data class DrinkPreviewResponse(
    val drinkId: Long,
    @field:Schema(example = "Капучино")
    val drinkName: String,
    val size: DrinkSize,
    val temperature: Temperature,
    @field:Schema(description = "Объём, мл", example = "450")
    val volumeMl: Int,
    @field:Schema(description = "Цена одной порции, ₽", example = "450")
    val unitPriceRubles: BigDecimal,
    val modifiers: List<AppliedModifierResponse>,
    @field:Schema(description = "Состав: ингредиенты и их количество")
    val composition: List<CompositionResponse>,
) {
    companion object {
        fun from(preview: DrinkPreview): DrinkPreviewResponse {
            val drink = preview.drink
            return DrinkPreviewResponse(
                drinkId = drink.drinkId,
                drinkName = drink.drinkName,
                size = drink.size,
                temperature = drink.temperature,
                volumeMl = drink.volumeMl,
                unitPriceRubles = drink.unitPrice.rubles,
                modifiers = drink.modifiers.map {
                    AppliedModifierResponse(it.modifierId, it.name, it.category, it.quantity, it.unitPrice.rubles, it.totalPrice.rubles)
                },
                composition = preview.composition.map { CompositionResponse(it.ingredientId, it.name, it.unit, it.quantity) },
            )
        }
    }
}

@Schema(description = "Модификатор в собранном напитке")
data class AppliedModifierResponse(
    val modifierId: Long,
    @field:Schema(example = "Сироп солёная карамель")
    val name: String,
    val category: ModifierCategory,
    val quantity: Int,
    @field:Schema(description = "Цена одной порции, ₽")
    val unitPriceRubles: BigDecimal,
    @field:Schema(description = "Цена всех порций, ₽")
    val totalPriceRubles: BigDecimal,
)

@Schema(description = "Ингредиент в составе напитка")
data class CompositionResponse(
    val ingredientId: Long,
    @field:Schema(example = "Овсяное молоко")
    val name: String,
    val unit: MeasureUnit,
    val quantity: Int,
)
