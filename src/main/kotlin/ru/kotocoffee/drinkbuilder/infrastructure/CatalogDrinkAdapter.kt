package ru.kotocoffee.drinkbuilder.infrastructure

import org.springframework.stereotype.Component
import ru.kotocoffee.catalog.CatalogFacade
import ru.kotocoffee.catalog.DrinkDefinition
import ru.kotocoffee.drinkbuilder.application.DrinkCatalogPort
import ru.kotocoffee.drinkbuilder.application.IngredientLabel
import ru.kotocoffee.drinkbuilder.domain.CategoryLimit
import ru.kotocoffee.drinkbuilder.domain.DrinkTemplate
import ru.kotocoffee.drinkbuilder.domain.IngredientPortion
import ru.kotocoffee.drinkbuilder.domain.ModifierOption
import ru.kotocoffee.drinkbuilder.domain.SizeVariant

@Component
class CatalogDrinkAdapter(private val catalogFacade: CatalogFacade) : DrinkCatalogPort {

    override fun getTemplate(drinkId: Long): DrinkTemplate = toTemplate(catalogFacade.getDrink(drinkId))

    override fun describeIngredients(ingredientIds: Collection<Long>): Map<Long, IngredientLabel> {
        val labels = mutableMapOf<Long, IngredientLabel>()
        for (ingredient in catalogFacade.getIngredients(ingredientIds)) {
            labels[ingredient.id] = IngredientLabel(ingredient.id, ingredient.name, ingredient.unit)
        }
        return labels
    }

    private fun toTemplate(definition: DrinkDefinition): DrinkTemplate =
        DrinkTemplate(
            drinkId = definition.id,
            name = definition.name,
            milkPortion = definition.milkPortion,
            defaultMilkModifierId = definition.defaultMilkModifierId,
            sizes = definition.sizes.map { SizeVariant(it.size, it.price, it.volumeMl, it.recipeScalePercent) },
            temperatures = definition.temperatures,
            recipe = definition.recipe.map { IngredientPortion(it.ingredientId, it.quantity) },
            limits = definition.modifierLimits.map { CategoryLimit(it.category, it.minCount, it.maxCount) },
            modifiers = definition.allowedModifiers.map {
                ModifierOption(it.id, it.name, it.category, it.price, it.ingredientId, it.quantityPerUnit)
            },
        )
}
