package ru.kotocoffee.ordering.api

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import ru.kotocoffee.ordering.application.PlaceOrderCommand
import ru.kotocoffee.ordering.application.PlaceOrderItem
import ru.kotocoffee.ordering.domain.DrinkSpec
import ru.kotocoffee.ordering.domain.ModifierSpec
import ru.kotocoffee.shared.kernel.DrinkSize
import ru.kotocoffee.shared.kernel.Temperature
import ru.kotocoffee.shared.web.RequestFields

@Schema(description = "Оформление заказа")
data class PlaceOrderRequest(
    @field:NotBlank
    @field:Size(max = 100)
    @field:Schema(description = "Имя, по которому покупатель заберёт заказ", example = "Мурзик")
    val customerName: String?,
    @field:NotEmpty
    @field:Size(max = 20)
    @field:Valid
    @field:Schema(description = "Собранные напитки")
    val items: List<OrderItemRequest?>?,
) {
    fun toCommand(): PlaceOrderCommand =
        PlaceOrderCommand(
            customerName = RequestFields.required(customerName, "customerName"),
            items = RequestFields.requiredElements(items, "items").map { it.toItem() },
        )
}

@Schema(description = "Напиток в заказе")
data class OrderItemRequest(
    @field:NotNull
    @field:Positive
    @field:Schema(description = "Базовый напиток", example = "3")
    val drinkId: Long?,
    @field:NotNull
    @field:Schema(example = "M")
    val size: DrinkSize?,
    @field:NotNull
    @field:Schema(example = "HOT")
    val temperature: Temperature?,
    @field:Valid
    @field:Size(max = 20)
    val modifiers: List<OrderModifierRequest?> = emptyList(),
    @field:Min(1)
    @field:Max(10)
    @field:Schema(description = "Количество одинаковых напитков", example = "1", defaultValue = "1")
    val quantity: Int = 1,
) {
    fun toItem(): PlaceOrderItem {
        val spec = DrinkSpec(
            drinkId = RequestFields.required(drinkId, "drinkId"),
            size = RequestFields.required(size, "size"),
            temperature = RequestFields.required(temperature, "temperature"),
            modifiers = RequestFields.requiredElements(modifiers, "modifiers").map { it.toSpec() },
        )
        return PlaceOrderItem(spec, quantity)
    }
}

@Schema(description = "Модификатор напитка в заказе")
data class OrderModifierRequest(
    @field:NotNull
    @field:Positive
    @field:Schema(example = "2")
    val modifierId: Long?,
    @field:Min(1)
    @field:Max(10)
    @field:Schema(example = "1", defaultValue = "1")
    val quantity: Int = 1,
) {
    fun toSpec(): ModifierSpec = ModifierSpec(RequestFields.required(modifierId, "modifierId"), quantity)
}
