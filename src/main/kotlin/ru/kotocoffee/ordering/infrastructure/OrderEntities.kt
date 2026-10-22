package ru.kotocoffee.ordering.infrastructure

import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.OrderColumn
import jakarta.persistence.Table
import jakarta.persistence.Version
import ru.kotocoffee.ordering.domain.OrderStatus
import ru.kotocoffee.shared.kernel.DrinkSize
import ru.kotocoffee.shared.kernel.ModifierCategory
import ru.kotocoffee.shared.kernel.Temperature
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "orders")
class OrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "customer_name")
    var customerName: String,
    @Enumerated(EnumType.STRING)
    var status: OrderStatus,
    @Column(name = "total_rubles")
    var totalRubles: BigDecimal,
    @Column(name = "created_at")
    var createdAt: Instant,
    @Column(name = "updated_at")
    var updatedAt: Instant,
    @Version
    var version: Long = 0,
    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("lineNo")
    var lines: MutableList<OrderLineEntity> = mutableListOf(),
    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("id")
    var statusHistory: MutableList<OrderStatusChangeEntity> = mutableListOf(),
)

@Entity
@Table(name = "order_lines")
class OrderLineEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    var order: OrderEntity,
    @Column(name = "line_no")
    var lineNo: Int,
    @Column(name = "base_drink_id")
    var drinkId: Long,
    @Column(name = "drink_name")
    var drinkName: String,
    @Enumerated(EnumType.STRING)
    var size: DrinkSize,
    @Enumerated(EnumType.STRING)
    var temperature: Temperature,
    @Column(name = "volume_ml")
    var volumeMl: Int,
    @Column(name = "unit_price_rubles")
    var unitPriceRubles: BigDecimal,
    var quantity: Int,
    @ElementCollection
    @CollectionTable(name = "order_line_modifiers", joinColumns = [JoinColumn(name = "order_line_id")])
    @OrderColumn(name = "position")
    var modifiers: MutableList<OrderLineModifierEmbeddable> = mutableListOf(),
)

@Embeddable
class OrderLineModifierEmbeddable(
    @Column(name = "modifier_id")
    var modifierId: Long,
    var name: String,
    @Enumerated(EnumType.STRING)
    var category: ModifierCategory,
    var quantity: Int,
    @Column(name = "unit_price_rubles")
    var unitPriceRubles: BigDecimal,
)

@Entity
@Table(name = "order_status_history")
class OrderStatusChangeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    var order: OrderEntity,
    @Enumerated(EnumType.STRING)
    var status: OrderStatus,
    @Column(name = "changed_at")
    var changedAt: Instant,
)
