package ru.kotocoffee.catalog.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.kotocoffee.catalog.domain.BaseDrink
import ru.kotocoffee.catalog.domain.Ingredient
import ru.kotocoffee.catalog.domain.MenuItem
import ru.kotocoffee.catalog.domain.Modifier
import ru.kotocoffee.shared.error.NotFoundException

@Service
@Transactional(readOnly = true)
class CatalogService(
    private val drinkRepository: BaseDrinkRepository,
    private val modifierRepository: ModifierRepository,
    private val ingredientRepository: IngredientRepository,
) {

    fun getMenu(): List<MenuItem> {
        val allModifiers = modifierRepository.findAllModifiers()
        val items = mutableListOf<MenuItem>()
        for (drink in drinkRepository.findAllDrinks()) {
            items.add(MenuItem(drink, allowedModifiersOf(drink, allModifiers)))
        }
        return items
    }

    fun getMenuItem(drinkId: Long): MenuItem {
        val drink = drinkRepository.findById(drinkId)
            ?: throw NotFoundException("Напиток $drinkId не найден в меню", "drink-not-found")
        return MenuItem(drink, allowedModifiersOf(drink, modifierRepository.findAllModifiers()))
    }

    fun getIngredients(ingredientIds: Collection<Long>): List<Ingredient> {
        if (ingredientIds.isEmpty()) {
            return emptyList()
        }
        return ingredientRepository.findByIds(ingredientIds)
    }

    private fun allowedModifiersOf(drink: BaseDrink, allModifiers: List<Modifier>): List<Modifier> {
        val allowedCategories = drink.allowedCategories()
        val allowed = mutableListOf<Modifier>()
        for (modifier in allModifiers) {
            if (modifier.category in allowedCategories) {
                allowed.add(modifier)
            }
        }
        return allowed
    }
}
