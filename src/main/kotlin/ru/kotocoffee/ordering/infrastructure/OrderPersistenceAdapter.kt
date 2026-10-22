package ru.kotocoffee.ordering.infrastructure

import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Component
import ru.kotocoffee.ordering.application.OrderRepository
import ru.kotocoffee.ordering.domain.Order
import ru.kotocoffee.ordering.domain.OrderLine
import ru.kotocoffee.ordering.domain.OrderLineModifier
import ru.kotocoffee.ordering.domain.OrderStatus
import ru.kotocoffee.ordering.domain.StatusChange
import ru.kotocoffee.shared.error.ConcurrentUpdateException
import ru.kotocoffee.shared.kernel.Money
import ru.kotocoffee.shared.kernel.PageQuery
import ru.kotocoffee.shared.kernel.PageResult

@Component
class OrderPersistenceAdapter(private val jpaRepository: OrderJpaRepository) : OrderRepository {

    override fun save(order: Order): Order {
        val orderId = order.id
        if (orderId == null) {
            return toDomain(jpaRepository.saveAndFlush(toNewEntity(order)))
        }

        val entity = jpaRepository.findByIdOrNull(orderId)
            ?: throw IllegalStateException("Заказ $orderId не найден при сохранении")
        if (entity.version != order.version) {
            throw concurrentUpdate(orderId)
        }
        entity.status = order.status
        entity.updatedAt = order.updatedAt
        val history = order.statusHistory
        for (index in entity.statusHistory.size until history.size) {
            val change = history[index]
            entity.statusHistory.add(OrderStatusChangeEntity(null, entity, change.status, change.changedAt))
        }
        try {
            return toDomain(jpaRepository.saveAndFlush(entity))
        } catch (exception: OptimisticLockingFailureException) {
            throw concurrentUpdate(orderId)
        }
    }

    private fun concurrentUpdate(orderId: Long) =
        ConcurrentUpdateException("Заказ $orderId был изменён другим запросом. Обновите данные и повторите попытку.")

    override fun findById(orderId: Long): Order? {
        val entity = jpaRepository.findByIdOrNull(orderId) ?: return null
        return toDomain(entity)
    }

    override fun findAll(statuses: Collection<OrderStatus>, page: PageQuery): PageResult<Order> {
        val pageable = PageRequest.of(page.page, page.size)
        val result = if (statuses.isEmpty()) {
            jpaRepository.findAllByOrderByCreatedAtDescIdDesc(pageable)
        } else {
            jpaRepository.findByStatusInOrderByCreatedAtDescIdDesc(statuses, pageable)
        }
        return PageResult(result.content.map { toDomain(it) }, page.page, page.size, result.totalElements)
    }

    private fun toNewEntity(order: Order): OrderEntity {
        val entity = OrderEntity(
            customerName = order.customerName,
            status = order.status,
            totalRubles = order.total.rubles,
            createdAt = order.createdAt,
            updatedAt = order.updatedAt,
        )
        var lineNo = 1
        for (line in order.lines) {
            val lineEntity = OrderLineEntity(
                order = entity,
                lineNo = lineNo,
                drinkId = line.drinkId,
                drinkName = line.drinkName,
                size = line.size,
                temperature = line.temperature,
                volumeMl = line.volumeMl,
                unitPriceRubles = line.unitPrice.rubles,
                quantity = line.quantity,
            )
            for (modifier in line.modifiers) {
                lineEntity.modifiers.add(
                    OrderLineModifierEmbeddable(modifier.modifierId, modifier.name, modifier.category, modifier.quantity, modifier.unitPrice.rubles),
                )
            }
            entity.lines.add(lineEntity)
            lineNo++
        }
        for (change in order.statusHistory) {
            entity.statusHistory.add(OrderStatusChangeEntity(null, entity, change.status, change.changedAt))
        }
        return entity
    }

    private fun toDomain(entity: OrderEntity): Order {
        val lines = mutableListOf<OrderLine>()
        for (lineEntity in entity.lines) {
            val modifiers = lineEntity.modifiers.map {
                OrderLineModifier(it.modifierId, it.name, it.category, it.quantity, Money(it.unitPriceRubles))
            }
            lines.add(
                OrderLine(
                    drinkId = lineEntity.drinkId,
                    drinkName = lineEntity.drinkName,
                    size = lineEntity.size,
                    temperature = lineEntity.temperature,
                    volumeMl = lineEntity.volumeMl,
                    modifiers = modifiers,
                    unitPrice = Money(lineEntity.unitPriceRubles),
                    quantity = lineEntity.quantity,
                ),
            )
        }
        val history = entity.statusHistory.map { StatusChange(it.status, it.changedAt) }
        return Order.restore(
            id = entity.id ?: throw IllegalStateException("Сущность заказа без идентификатора"),
            customerName = entity.customerName,
            status = entity.status,
            lines = lines,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            statusHistory = history,
            version = entity.version,
        )
    }
}
