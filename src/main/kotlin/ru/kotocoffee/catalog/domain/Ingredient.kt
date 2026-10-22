package ru.kotocoffee.catalog.domain

import ru.kotocoffee.shared.kernel.MeasureUnit

data class Ingredient(
    val id: Long,
    val code: String,
    val name: String,
    val unit: MeasureUnit,
)
