package ru.kotocoffee.ordering.domain

enum class OrderStatus(val description: String) {
    CREATED("В очереди"),
    ACCEPTED("Принят баристой"),
    IN_PROGRESS("Готовится"),
    READY("Готов к выдаче"),
    COMPLETED("Выдан"),
    CANCELLED("Отменён"),
    ;

    fun allowedTransitions(): Set<OrderStatus> =
        when (this) {
            CREATED -> setOf(ACCEPTED, CANCELLED)
            ACCEPTED -> setOf(IN_PROGRESS, CANCELLED)
            IN_PROGRESS -> setOf(READY)
            READY -> setOf(COMPLETED)
            COMPLETED -> emptySet()
            CANCELLED -> emptySet()
        }

    fun canTransitionTo(target: OrderStatus): Boolean = target in allowedTransitions()
}
