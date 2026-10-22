package ru.kotocoffee.drinkbuilder.application

import ru.kotocoffee.drinkbuilder.domain.DrinkTemplate
import ru.kotocoffee.shared.kernel.MeasureUnit

interface DrinkCatalogPort {
    fun getTemplate(drinkId: Long): DrinkTemplate

    fun describeIngredients(ingredientIds: Collection<Long>): Map<Long, IngredientLabel>
}

data class IngredientLabel(
    val id: Long,
    val name: String,
    val unit: MeasureUnit,
)
