package ru.kotocoffee.ordering.application

import ru.kotocoffee.ordering.domain.DrinkSpec

data class PlaceOrderCommand(
    val customerName: String,
    val items: List<PlaceOrderItem>,
)

data class PlaceOrderItem(
    val drink: DrinkSpec,
    val quantity: Int,
)
