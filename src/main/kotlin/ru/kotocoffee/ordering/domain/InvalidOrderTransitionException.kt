package ru.kotocoffee.ordering.domain

import ru.kotocoffee.shared.error.ConflictException

class InvalidOrderTransitionException(
    orderId: Long?,
    current: OrderStatus,
    target: OrderStatus,
) : ConflictException(
    "Заказ $orderId нельзя перевести из статуса $current в $target",
    "invalid-order-transition",
    mapOf("currentStatus" to current.name, "targetStatus" to target.name),
)
