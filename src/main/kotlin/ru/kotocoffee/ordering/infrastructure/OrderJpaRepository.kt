package ru.kotocoffee.ordering.infrastructure

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import ru.kotocoffee.ordering.domain.OrderStatus

interface OrderJpaRepository : JpaRepository<OrderEntity, Long> {
    fun findAllByOrderByCreatedAtDescIdDesc(pageable: Pageable): Page<OrderEntity>

    fun findByStatusInOrderByCreatedAtDescIdDesc(statuses: Collection<OrderStatus>, pageable: Pageable): Page<OrderEntity>
}
