package ru.kotocoffee.catalog.domain

import ru.kotocoffee.shared.kernel.ModifierCategory
import ru.kotocoffee.shared.kernel.Money

data class Modifier(
    val id: Long,
    val code: String,
    val name: String,
    val category: ModifierCategory,
    val price: Money,
    val ingredientId: Long,
    val quantityPerUnit: Int,
)
