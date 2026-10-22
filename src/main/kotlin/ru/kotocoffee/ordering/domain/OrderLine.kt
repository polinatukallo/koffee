package ru.kotocoffee.ordering.domain

import ru.kotocoffee.shared.error.BusinessRuleViolationException
import ru.kotocoffee.shared.kernel.DrinkSize
import ru.kotocoffee.shared.kernel.ModifierCategory
import ru.kotocoffee.shared.kernel.Money
import ru.kotocoffee.shared.kernel.Temperature

data class DrinkSpec(
    val drinkId: Long,
    val size: DrinkSize,
    val temperature: Temperature,
    val modifiers: List<ModifierSpec>,
)

data class ModifierSpec(
    val modifierId: Long,
    val quantity: Int,
)

data class OrderLine(
    val drinkId: Long,
    val drinkName: String,
    val size: DrinkSize,
    val temperature: Temperature,
    val volumeMl: Int,
    val modifiers: List<OrderLineModifier>,
    val unitPrice: Money,
    val quantity: Int,
) {
    init {
        if (quantity !in 1..MAX_QUANTITY) {
            throw BusinessRuleViolationException(
                "Количество одинаковых напитков в позиции должно быть от 1 до $MAX_QUANTITY",
                "invalid-line-quantity",
            )
        }
    }

    val lineTotal: Money
        get() = unitPrice * quantity

    companion object {
        const val MAX_QUANTITY = 10
    }
}

data class OrderLineModifier(
    val modifierId: Long,
    val name: String,
    val category: ModifierCategory,
    val quantity: Int,
    val unitPrice: Money,
)
