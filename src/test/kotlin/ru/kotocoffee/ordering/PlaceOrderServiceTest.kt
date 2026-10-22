package ru.kotocoffee.ordering

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.kotocoffee.ordering.OrderFixtures.NOW
import ru.kotocoffee.ordering.application.DrinkPricingPort
import ru.kotocoffee.ordering.application.OrderRepository
import ru.kotocoffee.ordering.application.PlaceOrderCommand
import ru.kotocoffee.ordering.application.PlaceOrderItem
import ru.kotocoffee.ordering.application.PlaceOrderService
import ru.kotocoffee.ordering.application.PricedDrinkSpec
import ru.kotocoffee.ordering.domain.DrinkSpec
import ru.kotocoffee.ordering.domain.Order
import ru.kotocoffee.shared.error.BusinessRuleViolationException
import ru.kotocoffee.shared.kernel.DrinkSize
import ru.kotocoffee.shared.kernel.Money
import ru.kotocoffee.shared.kernel.Temperature
import java.time.Clock
import java.time.ZoneOffset

class PlaceOrderServiceTest {

    private val orderRepository = mockk<OrderRepository>()
    private val drinkPricing = mockk<DrinkPricingPort>()
    private val service = PlaceOrderService(orderRepository, drinkPricing, Clock.fixed(NOW, ZoneOffset.UTC))

    private val latteSpec = DrinkSpec(4, DrinkSize.S, Temperature.HOT, emptyList())

    @Test
    fun `places order with priced line snapshots`() {
        every { drinkPricing.price(latteSpec) } returns pricedLatte()
        every { orderRepository.save(any()) } answers { withId(firstArg(), 42) }

        val order = service.placeOrder(PlaceOrderCommand("Мурзик", listOf(PlaceOrderItem(latteSpec, 3))))

        assertEquals(42L, order.id)
        assertEquals(Money(690), order.total)
        assertEquals("Латте", order.lines.first().drinkName)
        assertEquals(3, order.lines.first().quantity)
    }

    @Test
    fun `invalid drink configuration prevents saving the order`() {
        every { drinkPricing.price(latteSpec) } throws BusinessRuleViolationException("нельзя", "invalid-drink-configuration")

        assertThrows<BusinessRuleViolationException> {
            service.placeOrder(PlaceOrderCommand("Мурзик", listOf(PlaceOrderItem(latteSpec, 1))))
        }
        verify(exactly = 0) { orderRepository.save(any()) }
    }

    private fun pricedLatte() =
        PricedDrinkSpec(
            drinkId = 4,
            drinkName = "Латте",
            size = DrinkSize.S,
            temperature = Temperature.HOT,
            volumeMl = 300,
            unitPrice = Money(230),
            modifiers = emptyList(),
        )

    private fun withId(order: Order, id: Long): Order =
        Order.restore(
            id = id,
            customerName = order.customerName,
            status = order.status,
            lines = order.lines,
            createdAt = order.createdAt,
            updatedAt = order.updatedAt,
            statusHistory = order.statusHistory,
            version = 0,
        )
}
