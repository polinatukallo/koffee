package ru.kotocoffee.ordering

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.kotocoffee.ordering.OrderFixtures.NOW
import ru.kotocoffee.ordering.application.OrderAction
import ru.kotocoffee.ordering.application.OrderRepository
import ru.kotocoffee.ordering.application.OrderStatusService
import ru.kotocoffee.ordering.domain.InvalidOrderTransitionException
import ru.kotocoffee.ordering.domain.OrderStatus
import ru.kotocoffee.shared.error.NotFoundException
import java.time.Clock
import java.time.ZoneOffset

class OrderStatusServiceTest {

    private val orderRepository = mockk<OrderRepository>()
    private val service = OrderStatusService(orderRepository, Clock.fixed(NOW, ZoneOffset.UTC))

    @Test
    fun `applies the action and saves the order`() {
        val order = OrderFixtures.storedOrder(OrderStatus.READY)
        every { orderRepository.findById(10) } returns order
        every { orderRepository.save(order) } returns order

        val result = service.apply(10, OrderAction.COMPLETE)

        assertEquals(OrderStatus.COMPLETED, result.status)
        verify { orderRepository.save(order) }
    }

    @Test
    fun `invalid transition is rejected without saving`() {
        every { orderRepository.findById(10) } returns OrderFixtures.storedOrder(OrderStatus.IN_PROGRESS)

        assertThrows<InvalidOrderTransitionException> { service.apply(10, OrderAction.CANCEL) }
        verify(exactly = 0) { orderRepository.save(any()) }
    }

    @Test
    fun `unknown order returns not found`() {
        every { orderRepository.findById(99) } returns null

        assertThrows<NotFoundException> { service.apply(99, OrderAction.ACCEPT) }
    }
}
