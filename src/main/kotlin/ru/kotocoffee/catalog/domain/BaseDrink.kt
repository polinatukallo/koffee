package ru.kotocoffee.catalog.domain

import ru.kotocoffee.shared.kernel.DrinkSize
import ru.kotocoffee.shared.kernel.ModifierCategory
import ru.kotocoffee.shared.kernel.Money
import ru.kotocoffee.shared.kernel.Temperature

data class BaseDrink(
    val id: Long,
    val code: String,
    val name: String,
    val description: String,
    val milkPortion: Int,
    val defaultMilkModifierId: Long?,
    val sizes: List<DrinkSizeOption>,
    val temperatures: Set<Temperature>,
    val recipe: List<RecipeItem>,
    val modifierLimits: List<ModifierLimit>,
) {
    fun allowedCategories(): Set<ModifierCategory> {
        val categories = mutableSetOf<ModifierCategory>()
        for (limit in modifierLimits) {
            categories.add(limit.category)
        }
        return categories
    }

    fun startingPrice(): Money {
        var minimal: Money? = null
        for (option in sizes) {
            if (minimal == null || option.price < minimal) {
                minimal = option.price
            }
        }
        return minimal ?: Money.ZERO
    }
}

data class DrinkSizeOption(
    val size: DrinkSize,
    val price: Money,
    val volumeMl: Int,
    val recipeScalePercent: Int,
)

data class RecipeItem(
    val ingredientId: Long,
    val quantity: Int,
)

data class ModifierLimit(
    val category: ModifierCategory,
    val minCount: Int,
    val maxCount: Int,
)
