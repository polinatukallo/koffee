package ru.kotocoffee.ordering

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import ru.kotocoffee.ordering.OrderFixtures.NOW
import ru.kotocoffee.ordering.domain.InvalidOrderTransitionException
import ru.kotocoffee.ordering.domain.Order
import ru.kotocoffee.ordering.domain.OrderStatus
import ru.kotocoffee.shared.error.BusinessRuleViolationException
import ru.kotocoffee.shared.kernel.Money

class OrderTest {

    @Test
    fun `new order is created in the queue with total`() {
        val order = OrderFixtures.newOrder()

        assertEquals(OrderStatus.CREATED, order.status)
        assertEquals("Мурзик", order.customerName)
        assertEquals(Money(640), order.total)
        assertEquals(1, order.statusHistory.size)
        assertTrue(order.canBeCancelled())
    }

    @Test
    fun `order goes through the full happy path`() {
        val order = OrderFixtures.newOrder()

        order.accept(NOW)
        order.startPreparation(NOW)
        order.markReady(NOW)
        order.complete(NOW)

        assertEquals(OrderStatus.COMPLETED, order.status)
        val statuses = order.statusHistory.map { it.status }
        assertEquals(
            listOf(OrderStatus.CREATED, OrderStatus.ACCEPTED, OrderStatus.IN_PROGRESS, OrderStatus.READY, OrderStatus.COMPLETED),
            statuses,
        )
    }

    @Test
    fun `order can be cancelled before preparation started`() {
        val created = OrderFixtures.newOrder()
        created.cancel(NOW)
        assertEquals(OrderStatus.CANCELLED, created.status)

        val accepted = OrderFixtures.storedOrder(OrderStatus.ACCEPTED)
        accepted.cancel(NOW)
        assertEquals(OrderStatus.CANCELLED, accepted.status)
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus::class, names = ["IN_PROGRESS", "READY", "COMPLETED", "CANCELLED"])
    fun `order cannot be cancelled once preparation started`(status: OrderStatus) {
        val order = OrderFixtures.storedOrder(status)

        assertFalse(order.canBeCancelled())
        assertThrows<InvalidOrderTransitionException> { order.cancel(NOW) }
        assertEquals(status, order.status)
    }

    @Test
    fun `statuses cannot be skipped`() {
        val order = OrderFixtures.newOrder()

        assertThrows<InvalidOrderTransitionException> { order.markReady(NOW) }
        assertThrows<InvalidOrderTransitionException> { order.complete(NOW) }
        assertThrows<InvalidOrderTransitionException> { order.startPreparation(NOW) }
        assertEquals(OrderStatus.CREATED, order.status)
    }

    @Test
    fun `order must contain at least one line and a customer name`() {
        assertThrows<BusinessRuleViolationException> { Order.place("Мурзик", emptyList(), NOW) }
        assertThrows<BusinessRuleViolationException> { Order.place("   ", listOf(OrderFixtures.cappuccinoLine()), NOW) }
    }

    @Test
    fun `customer name is trimmed`() {
        assertEquals("Барсик", Order.place("  Барсик ", listOf(OrderFixtures.cappuccinoLine()), NOW).customerName)
    }

    @Test
    fun `line quantity is limited`() {
        assertThrows<BusinessRuleViolationException> { OrderFixtures.cappuccinoLine(0) }
        assertThrows<BusinessRuleViolationException> { OrderFixtures.cappuccinoLine(11) }
    }
}
