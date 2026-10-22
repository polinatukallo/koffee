package ru.kotocoffee.ordering.application

import ru.kotocoffee.ordering.domain.DrinkSpec
import ru.kotocoffee.ordering.domain.Order
import ru.kotocoffee.ordering.domain.OrderLineModifier
import ru.kotocoffee.ordering.domain.OrderStatus
import ru.kotocoffee.shared.kernel.DrinkSize
import ru.kotocoffee.shared.kernel.Money
import ru.kotocoffee.shared.kernel.PageQuery
import ru.kotocoffee.shared.kernel.PageResult
import ru.kotocoffee.shared.kernel.Temperature

interface OrderRepository {
    fun save(order: Order): Order

    fun findById(orderId: Long): Order?

    fun findAll(statuses: Collection<OrderStatus>, page: PageQuery): PageResult<Order>
}

interface DrinkPricingPort {
    fun price(spec: DrinkSpec): PricedDrinkSpec
}

data class PricedDrinkSpec(
    val drinkId: Long,
    val drinkName: String,
    val size: DrinkSize,
    val temperature: Temperature,
    val volumeMl: Int,
    val unitPrice: Money,
    val modifiers: List<OrderLineModifier>,
)
