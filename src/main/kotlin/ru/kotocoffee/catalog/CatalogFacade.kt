package ru.kotocoffee.catalog

import ru.kotocoffee.shared.kernel.DrinkSize
import ru.kotocoffee.shared.kernel.MeasureUnit
import ru.kotocoffee.shared.kernel.ModifierCategory
import ru.kotocoffee.shared.kernel.Money
import ru.kotocoffee.shared.kernel.Temperature

interface CatalogFacade {

    fun getDrink(drinkId: Long): DrinkDefinition

    fun getIngredients(ingredientIds: Collection<Long>): List<IngredientInfo>
}

data class IngredientInfo(
    val id: Long,
    val name: String,
    val unit: MeasureUnit,
)

data class DrinkDefinition(
    val id: Long,
    val name: String,
    val milkPortion: Int,
    val defaultMilkModifierId: Long?,
    val sizes: List<SizeDefinition>,
    val temperatures: Set<Temperature>,
    val recipe: List<RecipeIngredientDefinition>,
    val modifierLimits: List<ModifierLimitDefinition>,
    val allowedModifiers: List<ModifierDefinition>,
)

data class SizeDefinition(
    val size: DrinkSize,
    val price: Money,
    val volumeMl: Int,
    val recipeScalePercent: Int,
)

data class RecipeIngredientDefinition(
    val ingredientId: Long,
    val quantity: Int,
)

data class ModifierLimitDefinition(
    val category: ModifierCategory,
    val minCount: Int,
    val maxCount: Int,
)

data class ModifierDefinition(
    val id: Long,
    val name: String,
    val category: ModifierCategory,
    val price: Money,
    val ingredientId: Long,
    val quantityPerUnit: Int,
)
