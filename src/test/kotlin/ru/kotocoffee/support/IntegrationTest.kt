package ru.kotocoffee.support

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(PostgresTestcontainersConfig::class)
abstract class IntegrationTest {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    protected fun get(path: String): MvcResult = perform(MockMvcRequestBuilders.get(path), null)

    protected fun post(path: String, body: Any?): MvcResult = perform(MockMvcRequestBuilders.post(path), body)

    protected fun json(result: MvcResult): JsonNode = objectMapper.readTree(result.response.contentAsByteArray)

    protected fun placeCappuccinoOrder(customerName: String = "Мурзик"): Long {
        val result = post("/api/orders", cappuccinoOrder(customerName))
        check(result.response.status == 201) { result.response.contentAsString }
        return json(result).get("id").asLong()
    }

    protected fun cappuccinoOrder(customerName: String, quantity: Int = 1): Map<String, Any> =
        mapOf(
            "customerName" to customerName,
            "items" to listOf(mapOf("drinkId" to DRINK_CAPPUCCINO, "size" to "M", "temperature" to "HOT", "quantity" to quantity)),
        )

    private fun perform(builder: MockHttpServletRequestBuilder, body: Any?): MvcResult {
        if (body != null) {
            builder.contentType(MediaType.APPLICATION_JSON)
            builder.content(objectMapper.writeValueAsString(body))
        }
        return mockMvc.perform(builder).andReturn()
    }

    companion object {
        const val DRINK_ESPRESSO = 1L
        const val DRINK_CAPPUCCINO = 3L
        const val DRINK_MATCHA = 5L

        const val MODIFIER_OAT_MILK = 2L
        const val MODIFIER_VANILLA = 5L
        const val MODIFIER_SALTED_CARAMEL = 6L
        const val MODIFIER_EXTRA_SHOT = 8L

        const val INGREDIENT_OAT_MILK = 4L
    }
}
