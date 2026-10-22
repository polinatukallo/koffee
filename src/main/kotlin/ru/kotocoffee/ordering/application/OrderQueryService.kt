package ru.kotocoffee.ordering.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.kotocoffee.ordering.domain.Order
import ru.kotocoffee.ordering.domain.OrderStatus
import ru.kotocoffee.shared.error.NotFoundException
import ru.kotocoffee.shared.kernel.PageQuery
import ru.kotocoffee.shared.kernel.PageResult

@Service
@Transactional(readOnly = true)
class OrderQueryService(private val orderRepository: OrderRepository) {

    fun getOrder(orderId: Long): Order =
        orderRepository.findById(orderId) ?: throw NotFoundException("Заказ $orderId не найден", "order-not-found")

    fun getOrders(statuses: Collection<OrderStatus>, page: PageQuery): PageResult<Order> =
        orderRepository.findAll(statuses, page)
}
