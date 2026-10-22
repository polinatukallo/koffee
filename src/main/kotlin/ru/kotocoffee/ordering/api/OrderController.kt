package ru.kotocoffee.ordering.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import ru.kotocoffee.ordering.application.OrderAction
import ru.kotocoffee.ordering.application.OrderQueryService
import ru.kotocoffee.ordering.application.OrderStatusService
import ru.kotocoffee.ordering.application.PlaceOrderService
import ru.kotocoffee.ordering.domain.OrderStatus
import ru.kotocoffee.shared.kernel.PageQuery
import ru.kotocoffee.shared.web.PageResponse

@RestController
@RequestMapping("/api/orders")
@Tag(
    name = "Заказы",
    description = "Оформление заказа и перевод по статусам: CREATED → ACCEPTED → IN_PROGRESS → READY → COMPLETED; " +
        "CANCELLED — только из CREATED или ACCEPTED. Недопустимый переход или одновременное изменение — 409.",
)
class OrderController(
    private val placeOrderService: PlaceOrderService,
    private val orderQueryService: OrderQueryService,
    private val orderStatusService: OrderStatusService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Оформить заказ",
        description = "Каждый напиток проверяется и оценивается по текущему меню (422 при нарушении правил сборки).",
    )
    fun placeOrder(@Valid @RequestBody request: PlaceOrderRequest): OrderResponse =
        OrderResponse.from(placeOrderService.placeOrder(request.toCommand()))

    @GetMapping
    @Operation(summary = "Список заказов", description = "Сначала новые. Параметр status можно указать несколько раз")
    fun getOrders(
        @RequestParam(required = false) status: List<OrderStatus>?,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) size: Int,
    ): PageResponse<OrderResponse> {
        val result = orderQueryService.getOrders(status ?: emptyList(), PageQuery(page, size))
        return PageResponse.from(result) { OrderResponse.from(it) }
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Заказ по идентификатору")
    fun getOrder(@PathVariable orderId: Long): OrderResponse = OrderResponse.from(orderQueryService.getOrder(orderId))

    @PostMapping("/{orderId}/accept")
    @Operation(summary = "Принять заказ", description = "CREATED → ACCEPTED")
    fun accept(@PathVariable orderId: Long): OrderResponse = apply(orderId, OrderAction.ACCEPT)

    @PostMapping("/{orderId}/start")
    @Operation(summary = "Начать приготовление", description = "ACCEPTED → IN_PROGRESS. После этого отмена невозможна")
    fun start(@PathVariable orderId: Long): OrderResponse = apply(orderId, OrderAction.START_PREPARATION)

    @PostMapping("/{orderId}/ready")
    @Operation(summary = "Заказ готов", description = "IN_PROGRESS → READY")
    fun ready(@PathVariable orderId: Long): OrderResponse = apply(orderId, OrderAction.MARK_READY)

    @PostMapping("/{orderId}/complete")
    @Operation(summary = "Заказ выдан", description = "READY → COMPLETED")
    fun complete(@PathVariable orderId: Long): OrderResponse = apply(orderId, OrderAction.COMPLETE)

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Отменить заказ", description = "Только из CREATED или ACCEPTED, иначе 409")
    fun cancel(@PathVariable orderId: Long): OrderResponse = apply(orderId, OrderAction.CANCEL)

    private fun apply(orderId: Long, action: OrderAction): OrderResponse =
        OrderResponse.from(orderStatusService.apply(orderId, action))
}
