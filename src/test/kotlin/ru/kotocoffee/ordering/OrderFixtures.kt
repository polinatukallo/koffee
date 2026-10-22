package ru.kotocoffee.ordering

import ru.kotocoffee.ordering.domain.Order
import ru.kotocoffee.ordering.domain.OrderLine
import ru.kotocoffee.ordering.domain.OrderLineModifier
import ru.kotocoffee.ordering.domain.OrderStatus
import ru.kotocoffee.shared.kernel.DrinkSize
import ru.kotocoffee.shared.kernel.ModifierCategory
import ru.kotocoffee.shared.kernel.Money
import ru.kotocoffee.shared.kernel.Temperature
import java.time.Instant

object OrderFixtures {
    val NOW: Instant = Instant.parse("2026-10-22T10:00:00Z")

    fun cappuccinoLine(quantity: Int = 1): OrderLine =
        OrderLine(
            drinkId = 3,
            drinkName = "Капучино",
            size = DrinkSize.M,
            temperature = Temperature.HOT,
            volumeMl = 350,
            modifiers = listOf(OrderLineModifier(2, "Овсяное молоко", ModifierCategory.MILK, 1, Money(60))),
            unitPrice = Money(320),
            quantity = quantity,
        )

    fun newOrder(): Order = Order.place("Мурзик", listOf(cappuccinoLine(2)), NOW)

    fun storedOrder(status: OrderStatus, id: Long = 10): Order =
        Order.restore(
            id = id,
            customerName = "Мурзик",
            status = status,
            lines = listOf(cappuccinoLine(2)),
            createdAt = NOW,
            updatedAt = NOW,
            statusHistory = emptyList(),
            version = 3,
        )
}
