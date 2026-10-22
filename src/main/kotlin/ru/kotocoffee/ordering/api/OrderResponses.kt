package ru.kotocoffee.ordering.api

import io.swagger.v3.oas.annotations.media.Schema
import ru.kotocoffee.ordering.domain.Order
import ru.kotocoffee.ordering.domain.OrderLine
import ru.kotocoffee.ordering.domain.OrderStatus
import ru.kotocoffee.shared.kernel.DrinkSize
import ru.kotocoffee.shared.kernel.ModifierCategory
import ru.kotocoffee.shared.kernel.Temperature
import java.math.BigDecimal
import java.time.Instant

@Schema(description = "Заказ")
data class OrderResponse(
    val id: Long?,
    @field:Schema(description = "Имя для выдачи", example = "Мурзик")
    val customerName: String,
    @field:Schema(description = "CREATED, ACCEPTED, IN_PROGRESS, READY, COMPLETED, CANCELLED")
    val status: OrderStatus,
    @field:Schema(description = "Статус по-русски", example = "Готовится")
    val statusDescription: String,
    @field:Schema(description = "Можно ли ещё отменить заказ")
    val cancellable: Boolean,
    val lines: List<OrderLineResponse>,
    @field:Schema(description = "Сумма заказа, ₽", example = "520")
    val totalRubles: BigDecimal,
    val createdAt: Instant,
    val updatedAt: Instant,
    @field:Schema(description = "История смены статусов")
    val statusHistory: List<StatusChangeResponse>,
) {
    companion object {
        fun from(order: Order) =
            OrderResponse(
                id = order.id,
                customerName = order.customerName,
                status = order.status,
                statusDescription = order.status.description,
                cancellable = order.canBeCancelled(),
                lines = order.lines.map { OrderLineResponse.from(it) },
                totalRubles = order.total.rubles,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt,
                statusHistory = order.statusHistory.map { StatusChangeResponse(it.status, it.changedAt) },
            )
    }
}

@Schema(description = "Позиция заказа (снимок на момент оформления)")
data class OrderLineResponse(
    val drinkId: Long,
    @field:Schema(example = "Капучино")
    val drinkName: String,
    val size: DrinkSize,
    val temperature: Temperature,
    val volumeMl: Int,
    val modifiers: List<OrderLineModifierResponse>,
    @field:Schema(description = "Цена одной порции, ₽")
    val unitPriceRubles: BigDecimal,
    val quantity: Int,
    @field:Schema(description = "Стоимость позиции, ₽")
    val lineTotalRubles: BigDecimal,
) {
    companion object {
        fun from(line: OrderLine) =
            OrderLineResponse(
                drinkId = line.drinkId,
                drinkName = line.drinkName,
                size = line.size,
                temperature = line.temperature,
                volumeMl = line.volumeMl,
                modifiers = line.modifiers.map {
                    OrderLineModifierResponse(it.modifierId, it.name, it.category, it.quantity, it.unitPrice.rubles)
                },
                unitPriceRubles = line.unitPrice.rubles,
                quantity = line.quantity,
                lineTotalRubles = line.lineTotal.rubles,
            )
    }
}

data class OrderLineModifierResponse(
    val modifierId: Long,
    val name: String,
    val category: ModifierCategory,
    val quantity: Int,
    val unitPriceRubles: BigDecimal,
)

data class StatusChangeResponse(
    val status: OrderStatus,
    val changedAt: Instant,
)
