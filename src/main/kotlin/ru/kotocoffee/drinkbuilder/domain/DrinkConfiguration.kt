package ru.kotocoffee.drinkbuilder.domain

import ru.kotocoffee.shared.kernel.DrinkSize
import ru.kotocoffee.shared.kernel.Temperature

data class DrinkConfiguration(
    val drinkId: Long,
    val size: DrinkSize,
    val temperature: Temperature,
    val modifiers: List<ModifierChoice>,
)

data class ModifierChoice(
    val modifierId: Long,
    val quantity: Int,
)
