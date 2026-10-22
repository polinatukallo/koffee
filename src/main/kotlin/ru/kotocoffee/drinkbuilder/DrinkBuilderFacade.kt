package ru.kotocoffee.drinkbuilder

import ru.kotocoffee.shared.kernel.DrinkSize
import ru.kotocoffee.shared.kernel.ModifierCategory
import ru.kotocoffee.shared.kernel.Money
import ru.kotocoffee.shared.kernel.Temperature

interface DrinkBuilderFacade {

    fun build(request: DrinkBuildRequest): PricedDrink
}

data class DrinkBuildRequest(
    val drinkId: Long,
    val size: DrinkSize,
    val temperature: Temperature,
    val modifiers: List<ModifierSelection>,
)

data class ModifierSelection(
    val modifierId: Long,
    val quantity: Int,
)

data class PricedDrink(
    val drinkId: Long,
    val drinkName: String,
    val size: DrinkSize,
    val temperature: Temperature,
    val volumeMl: Int,
    val unitPrice: Money,
    val modifiers: List<PricedModifier>,
)

data class PricedModifier(
    val modifierId: Long,
    val name: String,
    val category: ModifierCategory,
    val quantity: Int,
    val unitPrice: Money,
)
