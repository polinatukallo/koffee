package ru.kotocoffee.ordering.application

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.kotocoffee.ordering.domain.Order
import ru.kotocoffee.ordering.domain.OrderLine
import java.time.Clock

@Service
class PlaceOrderService(
    private val orderRepository: OrderRepository,
    private val drinkPricing: DrinkPricingPort,
    private val clock: Clock,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun placeOrder(command: PlaceOrderCommand): Order {
        val lines = mutableListOf<OrderLine>()
        for (item in command.items) {
            val priced = drinkPricing.price(item.drink)
            lines.add(
                OrderLine(
                    drinkId = priced.drinkId,
                    drinkName = priced.drinkName,
                    size = priced.size,
                    temperature = priced.temperature,
                    volumeMl = priced.volumeMl,
                    modifiers = priced.modifiers,
                    unitPrice = priced.unitPrice,
                    quantity = item.quantity,
                ),
            )
        }
        val order = Order.place(command.customerName, lines, clock.instant())
        val saved = orderRepository.save(order)
        log.info("Order {} placed: lines {}, total {}", saved.id, lines.size, saved.total.rubles)
        return saved
    }
}
