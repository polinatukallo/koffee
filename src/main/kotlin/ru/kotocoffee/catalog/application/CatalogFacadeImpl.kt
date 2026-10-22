package ru.kotocoffee.catalog.application

import org.springframework.stereotype.Component
import ru.kotocoffee.catalog.CatalogFacade
import ru.kotocoffee.catalog.DrinkDefinition
import ru.kotocoffee.catalog.IngredientInfo
import ru.kotocoffee.catalog.ModifierDefinition
import ru.kotocoffee.catalog.ModifierLimitDefinition
import ru.kotocoffee.catalog.RecipeIngredientDefinition
import ru.kotocoffee.catalog.SizeDefinition
import ru.kotocoffee.catalog.domain.MenuItem

@Component
class CatalogFacadeImpl(private val catalogService: CatalogService) : CatalogFacade {

    override fun getDrink(drinkId: Long): DrinkDefinition = toDefinition(catalogService.getMenuItem(drinkId))

    override fun getIngredients(ingredientIds: Collection<Long>): List<IngredientInfo> =
        catalogService.getIngredients(ingredientIds).map { IngredientInfo(it.id, it.name, it.unit) }

    private fun toDefinition(menuItem: MenuItem): DrinkDefinition {
        val drink = menuItem.drink
        return DrinkDefinition(
            id = drink.id,
            name = drink.name,
            milkPortion = drink.milkPortion,
            defaultMilkModifierId = drink.defaultMilkModifierId,
            sizes = drink.sizes.map { SizeDefinition(it.size, it.price, it.volumeMl, it.recipeScalePercent) },
            temperatures = drink.temperatures,
            recipe = drink.recipe.map { RecipeIngredientDefinition(it.ingredientId, it.quantity) },
            modifierLimits = drink.modifierLimits.map { ModifierLimitDefinition(it.category, it.minCount, it.maxCount) },
            allowedModifiers = menuItem.allowedModifiers.map {
                ModifierDefinition(it.id, it.name, it.category, it.price, it.ingredientId, it.quantityPerUnit)
            },
        )
    }
}
