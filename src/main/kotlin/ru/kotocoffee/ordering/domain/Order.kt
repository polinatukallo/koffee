package ru.kotocoffee.ordering.domain

import ru.kotocoffee.shared.error.BusinessRuleViolationException
import ru.kotocoffee.shared.kernel.Money
import java.time.Instant

data class StatusChange(
    val status: OrderStatus,
    val changedAt: Instant,
)

class Order private constructor(
    val id: Long?,
    val customerName: String,
    status: OrderStatus,
    val lines: List<OrderLine>,
    val createdAt: Instant,
    updatedAt: Instant,
    statusHistory: List<StatusChange>,
    val version: Long,
) {
    var status: OrderStatus = status
        private set

    var updatedAt: Instant = updatedAt
        private set

    private val history: MutableList<StatusChange> = statusHistory.toMutableList()

    val statusHistory: List<StatusChange>
        get() = history.toList()

    val total: Money
        get() {
            var sum = Money.ZERO
            for (line in lines) {
                sum += line.lineTotal
            }
            return sum
        }

    fun canBeCancelled(): Boolean = status.canTransitionTo(OrderStatus.CANCELLED)

    fun accept(at: Instant) = transitionTo(OrderStatus.ACCEPTED, at)

    fun startPreparation(at: Instant) = transitionTo(OrderStatus.IN_PROGRESS, at)

    fun markReady(at: Instant) = transitionTo(OrderStatus.READY, at)

    fun complete(at: Instant) = transitionTo(OrderStatus.COMPLETED, at)

    fun cancel(at: Instant) = transitionTo(OrderStatus.CANCELLED, at)

    private fun transitionTo(target: OrderStatus, at: Instant) {
        if (!status.canTransitionTo(target)) {
            throw InvalidOrderTransitionException(id, status, target)
        }
        status = target
        updatedAt = at
        history.add(StatusChange(target, at))
    }

    companion object {
        const val MAX_LINES = 20

        fun place(customerName: String, lines: List<OrderLine>, now: Instant): Order {
            val name = customerName.trim()
            if (name.isEmpty()) {
                throw BusinessRuleViolationException("Укажите имя для выдачи заказа", "customer-name-required")
            }
            if (lines.isEmpty()) {
                throw BusinessRuleViolationException("Заказ должен содержать хотя бы один напиток", "empty-order")
            }
            if (lines.size > MAX_LINES) {
                throw BusinessRuleViolationException("В заказе может быть не больше $MAX_LINES позиций", "too-many-lines")
            }
            val history = listOf(StatusChange(OrderStatus.CREATED, now))
            return Order(null, name, OrderStatus.CREATED, lines, now, now, history, 0)
        }

        fun restore(
            id: Long,
            customerName: String,
            status: OrderStatus,
            lines: List<OrderLine>,
            createdAt: Instant,
            updatedAt: Instant,
            statusHistory: List<StatusChange>,
            version: Long,
        ): Order = Order(id, customerName, status, lines, createdAt, updatedAt, statusHistory, version)
    }
}
