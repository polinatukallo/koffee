package ru.kotocoffee.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.kotocoffee.support.IntegrationTest

class OrderFlowIntegrationTest : IntegrationTest() {

    @Test
    fun `order goes from placement to handing out`() {
        val placed = post("/api/orders", cappuccinoOrder("Барсик", quantity = 2))

        assertEquals(201, placed.response.status)
        val order = json(placed)
        val orderId = order.get("id").asLong()
        assertEquals("CREATED", order.get("status").asText())
        assertEquals("Барсик", order.get("customerName").asText())
        assertEquals(520, order.get("totalRubles").asLong())
        assertEquals("Коровье молоко", order.get("lines").get(0).get("modifiers").get(0).get("name").asText())

        assertEquals("ACCEPTED", json(post("/api/orders/$orderId/accept", null)).get("status").asText())
        assertEquals("IN_PROGRESS", json(post("/api/orders/$orderId/start", null)).get("status").asText())
        assertEquals("READY", json(post("/api/orders/$orderId/ready", null)).get("status").asText())
        val completed = json(post("/api/orders/$orderId/complete", null))
        assertEquals("COMPLETED", completed.get("status").asText())
        assertFalse(completed.get("cancellable").asBoolean())
        assertEquals(5, completed.get("statusHistory").size())

        assertEquals("COMPLETED", json(get("/api/orders/$orderId")).get("status").asText())
    }

    @Test
    fun `order can be cancelled before preparation started`() {
        val orderId = placeCappuccinoOrder()
        post("/api/orders/$orderId/accept", null)

        val cancelled = post("/api/orders/$orderId/cancel", null)

        assertEquals(200, cancelled.response.status)
        assertEquals("CANCELLED", json(cancelled).get("status").asText())
    }

    @Test
    fun `order cannot be cancelled after preparation started`() {
        val orderId = placeCappuccinoOrder()
        post("/api/orders/$orderId/accept", null)
        post("/api/orders/$orderId/start", null)

        val result = post("/api/orders/$orderId/cancel", null)

        assertEquals(409, result.response.status)
        assertEquals("invalid-order-transition", json(result).get("code").asText())
    }

    @Test
    fun `statuses cannot be skipped`() {
        val orderId = placeCappuccinoOrder()

        val result = post("/api/orders/$orderId/complete", null)

        assertEquals(409, result.response.status)
        val body = json(result)
        assertEquals("CREATED", body.get("currentStatus").asText())
        assertEquals("COMPLETED", body.get("targetStatus").asText())
    }

    @Test
    fun `unknown order returns 404`() {
        assertEquals(404, get("/api/orders/999999").response.status)
        assertEquals("order-not-found", json(post("/api/orders/999999/accept", null)).get("code").asText())
    }

    @Test
    fun `invalid drink in the order returns 422 and nothing is saved`() {
        val before = json(get("/api/orders?size=1")).get("totalElements").asLong()
        val request = mapOf(
            "customerName" to "Мурзик",
            "items" to listOf(mapOf("drinkId" to DRINK_ESPRESSO, "size" to "L", "temperature" to "HOT")),
        )

        val result = post("/api/orders", request)

        assertEquals(422, result.response.status)
        assertEquals(before, json(get("/api/orders?size=1")).get("totalElements").asLong())
    }

    @Test
    fun `orders list is paged and filtered by status`() {
        val orderId = placeCappuccinoOrder("Фильтр")
        post("/api/orders/$orderId/accept", null)

        val accepted = json(get("/api/orders?status=ACCEPTED&size=100")).get("items")
        assertTrue(accepted.any { it.get("id").asLong() == orderId })
        for (order in accepted) {
            assertEquals("ACCEPTED", order.get("status").asText())
        }

        val firstPage = json(get("/api/orders?page=0&size=1"))
        assertEquals(1, firstPage.get("items").size())
        assertTrue(firstPage.get("totalElements").asLong() >= 1)
    }
}
