package ru.kotocoffee.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.kotocoffee.support.IntegrationTest

class CatalogAndBuilderIntegrationTest : IntegrationTest() {

    @Test
    fun `lists the menu with drink options`() {
        val menu = json(get("/api/menu"))
        assertEquals(5, menu.size())

        val cappuccino = menu.first { it.get("id").asLong() == DRINK_CAPPUCCINO }
        assertEquals(3, cappuccino.get("sizes").size())
        assertTrue(cappuccino.get("modifiers").size() > 0)

        val matcha = json(get("/api/menu/drinks/$DRINK_MATCHA"))
        assertEquals("Матча латте", matcha.get("name").asText())
        assertEquals(2, matcha.get("sizes").size())
    }

    @Test
    fun `unknown drink returns 404 problem`() {
        val result = get("/api/menu/drinks/999")

        assertEquals(404, result.response.status)
        assertEquals("drink-not-found", json(result).get("code").asText())
    }

    @Test
    fun `previews the drink from the product description`() {
        val request = mapOf(
            "drinkId" to DRINK_CAPPUCCINO,
            "size" to "L",
            "temperature" to "ICED",
            "modifiers" to listOf(
                mapOf("modifierId" to MODIFIER_OAT_MILK, "quantity" to 1),
                mapOf("modifierId" to MODIFIER_SALTED_CARAMEL, "quantity" to 1),
                mapOf("modifierId" to MODIFIER_VANILLA, "quantity" to 1),
                mapOf("modifierId" to MODIFIER_EXTRA_SHOT, "quantity" to 1),
            ),
        )

        val result = post("/api/drinks/preview", request)

        assertEquals(200, result.response.status)
        val body = json(result)
        assertEquals(490, body.get("unitPriceRubles").asLong())
        assertEquals(450, body.get("volumeMl").asInt())
        val oatMilk = body.get("composition").first { it.get("ingredientId").asLong() == INGREDIENT_OAT_MILK }
        assertEquals(270, oatMilk.get("quantity").asInt())
        assertEquals("Овсяное молоко", oatMilk.get("name").asText())
    }

    @Test
    fun `incompatible configuration returns 422 with all violations`() {
        val request = mapOf(
            "drinkId" to DRINK_ESPRESSO,
            "size" to "L",
            "temperature" to "HOT",
            "modifiers" to listOf(mapOf("modifierId" to MODIFIER_OAT_MILK, "quantity" to 1)),
        )

        val result = post("/api/drinks/preview", request)

        assertEquals(422, result.response.status)
        val body = json(result)
        assertEquals("invalid-drink-configuration", body.get("code").asText())
        assertEquals(2, body.get("violations").size())
    }

    @Test
    fun `swagger documentation and health are available`() {
        assertEquals(200, get("/v3/api-docs").response.status)
        assertEquals(200, get("/swagger-ui/index.html").response.status)
        assertEquals(200, get("/actuator/health").response.status)
    }
}
