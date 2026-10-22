package ru.kotocoffee.integration

import com.fasterxml.jackson.databind.JsonNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import ru.kotocoffee.support.IntegrationTest

class RequestValidationIntegrationTest : IntegrationTest() {

    private val validItem = mapOf("drinkId" to DRINK_ESPRESSO, "size" to "S", "temperature" to "HOT")

    @Test
    fun `invalid order requests are rejected with 400`() {
        assertValidationError(post("/api/orders", mapOf("customerName" to "Кот", "items" to emptyList<Any>())), "items")
        assertValidationError(post("/api/orders", mapOf("customerName" to "Кот", "items" to listOf(validItem + ("quantity" to 0)))), "items[0].quantity")
        assertValidationError(post("/api/orders", mapOf("customerName" to "Кот", "items" to listOf(validItem + ("quantity" to 11)))), "items[0].quantity")
        assertValidationError(post("/api/orders", mapOf("items" to listOf(validItem))), "customerName")
        assertValidationError(post("/api/orders", mapOf("customerName" to "   ", "items" to listOf(validItem))), "customerName")
        assertValidationError(post("/api/orders", mapOf("customerName" to "к".repeat(101), "items" to listOf(validItem))), "customerName")
    }

    @Test
    fun `null elements in request lists are rejected with 400`() {
        assertValidationError(post("/api/orders", mapOf("customerName" to "Кот", "items" to listOf(null))), "items[0]")

        val itemWithNullModifier = mapOf(
            "drinkId" to DRINK_CAPPUCCINO,
            "size" to "S",
            "temperature" to "HOT",
            "modifiers" to listOf(null),
        )
        assertValidationError(post("/api/orders", mapOf("customerName" to "Кот", "items" to listOf(itemWithNullModifier))), "modifiers[0]")
        assertValidationError(post("/api/drinks/preview", itemWithNullModifier), "modifiers[0]")
    }

    @Test
    fun `invalid paging parameters are rejected with 400`() {
        assertValidationError(get("/api/orders?page=-1"), "page")
        assertValidationError(get("/api/orders?size=1000"), "size")
        assertValidationError(get("/api/orders?size=0"), "size")
    }

    @Test
    fun `standard Spring MVC errors carry a problem code`() {
        val malformed = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("{not json"),
        ).andReturn()
        assertProblem(malformed, 400, "malformed-request")

        assertProblem(get("/api/orders/abc"), 400, "type-mismatch")
        assertProblem(get("/api/orders?status=FOO"), 400, "type-mismatch")

        val wrongMethod = mockMvc.perform(MockMvcRequestBuilders.delete("/api/orders")).andReturn()
        assertProblem(wrongMethod, 405, "method-not-allowed")
    }

    private fun assertValidationError(result: MvcResult, field: String) {
        val body = assertProblem(result, 400, "validation-failed")
        val fields = body.get("errors").map { it.get("field").asText() }
        assertTrue(fields.contains(field), "expected error for $field, got $fields")
    }

    private fun assertProblem(result: MvcResult, status: Int, code: String): JsonNode {
        assertEquals(status, result.response.status, result.response.contentAsString)
        val body = json(result)
        assertEquals(code, body.get("code").asText())
        assertEquals("https://kotocoffee.ru/problems/$code", body.get("type").asText())
        return body
    }
}
