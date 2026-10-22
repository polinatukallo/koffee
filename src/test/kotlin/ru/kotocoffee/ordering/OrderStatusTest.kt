package ru.kotocoffee.ordering

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.kotocoffee.ordering.domain.OrderStatus

class OrderStatusTest {

    @Test
    fun `transition table matches the business process`() {
        assertEquals(setOf(OrderStatus.ACCEPTED, OrderStatus.CANCELLED), OrderStatus.CREATED.allowedTransitions())
        assertEquals(setOf(OrderStatus.IN_PROGRESS, OrderStatus.CANCELLED), OrderStatus.ACCEPTED.allowedTransitions())
        assertEquals(setOf(OrderStatus.READY), OrderStatus.IN_PROGRESS.allowedTransitions())
        assertEquals(setOf(OrderStatus.COMPLETED), OrderStatus.READY.allowedTransitions())
        assertTrue(OrderStatus.COMPLETED.allowedTransitions().isEmpty())
        assertTrue(OrderStatus.CANCELLED.allowedTransitions().isEmpty())
    }
}
