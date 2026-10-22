package ru.kotocoffee.drinkbuilder.domain

import ru.kotocoffee.shared.kernel.DrinkSize
import ru.kotocoffee.shared.kernel.ModifierCategory
import ru.kotocoffee.shared.kernel.Money
import ru.kotocoffee.shared.kernel.Temperature

data class BuiltDrink(
    val drinkId: Long,
    val drinkName: String,
    val size: DrinkSize,
    val temperature: Temperature,
    val volumeMl: Int,
    val unitPrice: Money,
    val modifiers: List<AppliedModifier>,
    val composition: List<IngredientPortion>,
)

data class AppliedModifier(
    val modifierId: Long,
    val name: String,
    val category: ModifierCategory,
    val quantity: Int,
    val unitPrice: Money,
) {
    val totalPrice: Money
        get() = unitPrice * quantity
}
