package ru.kotocoffee.catalog.domain

data class MenuItem(
    val drink: BaseDrink,
    val allowedModifiers: List<Modifier>,
)
