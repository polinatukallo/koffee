package ru.kotocoffee.catalog.application

import ru.kotocoffee.catalog.domain.BaseDrink
import ru.kotocoffee.catalog.domain.Ingredient
import ru.kotocoffee.catalog.domain.Modifier

interface BaseDrinkRepository {
    fun findAllDrinks(): List<BaseDrink>

    fun findById(drinkId: Long): BaseDrink?
}

interface ModifierRepository {
    fun findAllModifiers(): List<Modifier>
}

interface IngredientRepository {
    fun findByIds(ingredientIds: Collection<Long>): List<Ingredient>
}
