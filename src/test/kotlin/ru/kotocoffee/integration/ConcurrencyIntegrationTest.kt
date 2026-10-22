package ru.kotocoffee.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.OptimisticLockingFailureException
import ru.kotocoffee.ordering.application.OrderAction
import ru.kotocoffee.ordering.application.OrderQueryService
import ru.kotocoffee.ordering.application.OrderStatusService
import ru.kotocoffee.ordering.domain.OrderStatus
import ru.kotocoffee.shared.error.BusinessException
import ru.kotocoffee.support.IntegrationTest
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ConcurrencyIntegrationTest : IntegrationTest() {

    @Autowired
    private lateinit var orderStatusService: OrderStatusService

    @Autowired
    private lateinit var orderQueryService: OrderQueryService

    @Test
    fun `the same order cannot be accepted twice concurrently`() {
        val orderId = placeCappuccinoOrder()
        val tasks = mutableListOf<Callable<Boolean>>()
        for (attempt in 1..4) {
            tasks.add(
                Callable {
                    orderStatusService.apply(orderId, OrderAction.ACCEPT)
                    true
                },
            )
        }

        assertEquals(1, runConcurrently(tasks))
        assertEquals(OrderStatus.ACCEPTED, orderQueryService.getOrder(orderId).status)
    }

    @Test
    fun `cancel races with start of preparation - exactly one wins`() {
        for (round in 1..5) {
            val orderId = placeCappuccinoOrder()
            orderStatusService.apply(orderId, OrderAction.ACCEPT)
            val tasks = listOf(
                Callable {
                    orderStatusService.apply(orderId, OrderAction.CANCEL)
                    true
                },
                Callable {
                    orderStatusService.apply(orderId, OrderAction.START_PREPARATION)
                    true
                },
            )

            assertEquals(1, runConcurrently(tasks), "round $round")
            val status = orderQueryService.getOrder(orderId).status
            val expected = status == OrderStatus.CANCELLED || status == OrderStatus.IN_PROGRESS
            assertEquals(true, expected, "round $round: unexpected status $status")
        }
    }

    private fun runConcurrently(tasks: List<Callable<Boolean>>): Int {
        val executor = Executors.newFixedThreadPool(tasks.size)
        val startSignal = CountDownLatch(1)
        try {
            val futures = tasks.map { task ->
                executor.submit(
                    Callable {
                        startSignal.await()
                        task.call()
                    },
                )
            }
            startSignal.countDown()
            var succeeded = 0
            for (future in futures) {
                try {
                    if (future.get(30, TimeUnit.SECONDS)) {
                        succeeded++
                    }
                } catch (exception: ExecutionException) {
                    val cause = exception.cause
                    val expected = cause is BusinessException || cause is OptimisticLockingFailureException
                    if (!expected) {
                        throw exception
                    }
                }
            }
            return succeeded
        } finally {
            executor.shutdownNow()
        }
    }
}
