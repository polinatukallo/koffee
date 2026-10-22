package ru.kotocoffee.shared

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.kotocoffee.shared.kernel.Money
import java.math.BigDecimal

class MoneyTest {

    @Test
    fun `money arithmetic`() {
        assertEquals(Money(350), Money(100) + Money(250))
        assertEquals(Money(300), Money(100) * 3)
        assertEquals(Money(BigDecimal("40.50")), Money(BigDecimal("13.50")) * 3)
        assertThrows<IllegalArgumentException> { Money(-1) }
    }

    @Test
    fun `rubles are kept with two decimal places`() {
        assertEquals(BigDecimal("490.00"), Money(490).rubles)
        assertEquals(Money(BigDecimal("490.0")), Money(490))
        assertThrows<ArithmeticException> { Money(BigDecimal("1.005")) }
    }
}
