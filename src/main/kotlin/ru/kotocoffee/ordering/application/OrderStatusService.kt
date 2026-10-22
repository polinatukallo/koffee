package ru.kotocoffee.ordering.application

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.kotocoffee.ordering.domain.Order
import ru.kotocoffee.shared.error.NotFoundException
import java.time.Clock

enum class OrderAction {
    ACCEPT,
    START_PREPARATION,
    MARK_READY,
    COMPLETE,
    CANCEL,
}

@Service
class OrderStatusService(
    private val orderRepository: OrderRepository,
    private val clock: Clock,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun apply(orderId: Long, action: OrderAction): Order {
        val order = orderRepository.findById(orderId)
            ?: throw NotFoundException("Заказ $orderId не найден", "order-not-found")
        val previousStatus = order.status
        val now = clock.instant()
        when (action) {
            OrderAction.ACCEPT -> order.accept(now)
            OrderAction.START_PREPARATION -> order.startPreparation(now)
            OrderAction.MARK_READY -> order.markReady(now)
            OrderAction.COMPLETE -> order.complete(now)
            OrderAction.CANCEL -> order.cancel(now)
        }
        val saved = orderRepository.save(order)
        log.info("Order {} status changed {} -> {}", orderId, previousStatus, saved.status)
        return saved
    }
}
