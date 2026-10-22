package ru.kotocoffee.drinkbuilder.domain

import ru.kotocoffee.shared.kernel.DrinkSize
import ru.kotocoffee.shared.kernel.ModifierCategory
import ru.kotocoffee.shared.kernel.Money
import ru.kotocoffee.shared.kernel.Temperature

data class DrinkTemplate(
    val drinkId: Long,
    val name: String,
    val milkPortion: Int,
    val defaultMilkModifierId: Long?,
    val sizes: List<SizeVariant>,
    val temperatures: Set<Temperature>,
    val recipe: List<IngredientPortion>,
    val limits: List<CategoryLimit>,
    val modifiers: List<ModifierOption>,
) {
    fun findSize(size: DrinkSize): SizeVariant? {
        for (variant in sizes) {
            if (variant.size == size) {
                return variant
            }
        }
        return null
    }

    fun findModifier(modifierId: Long): ModifierOption? {
        for (modifier in modifiers) {
            if (modifier.id == modifierId) {
                return modifier
            }
        }
        return null
    }

    fun findLimit(category: ModifierCategory): CategoryLimit? {
        for (limit in limits) {
            if (limit.category == category) {
                return limit
            }
        }
        return null
    }
}

data class SizeVariant(
    val size: DrinkSize,
    val price: Money,
    val volumeMl: Int,
    val recipeScalePercent: Int,
) {
    fun scale(quantity: Int): Int = (quantity * recipeScalePercent + 50) / 100
}

data class IngredientPortion(
    val ingredientId: Long,
    val quantity: Int,
)

data class CategoryLimit(
    val category: ModifierCategory,
    val minCount: Int,
    val maxCount: Int,
)

data class ModifierOption(
    val id: Long,
    val name: String,
    val category: ModifierCategory,
    val price: Money,
    val ingredientId: Long,
    val quantityPerUnit: Int,
)
