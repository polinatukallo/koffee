package ru.kotocoffee.catalog.infrastructure

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import ru.kotocoffee.catalog.application.BaseDrinkRepository
import ru.kotocoffee.catalog.application.IngredientRepository
import ru.kotocoffee.catalog.application.ModifierRepository
import ru.kotocoffee.catalog.domain.BaseDrink
import ru.kotocoffee.catalog.domain.DrinkSizeOption
import ru.kotocoffee.catalog.domain.Ingredient
import ru.kotocoffee.catalog.domain.Modifier
import ru.kotocoffee.catalog.domain.ModifierLimit
import ru.kotocoffee.catalog.domain.RecipeItem
import ru.kotocoffee.shared.kernel.Money

@Component
class CatalogPersistenceAdapter(
    private val drinkJpaRepository: BaseDrinkJpaRepository,
    private val modifierJpaRepository: ModifierJpaRepository,
    private val ingredientJpaRepository: IngredientJpaRepository,
) : BaseDrinkRepository, ModifierRepository, IngredientRepository {

    override fun findAllDrinks(): List<BaseDrink> =
        drinkJpaRepository.findAllByOrderById().map { toDomain(it) }

    override fun findById(drinkId: Long): BaseDrink? {
        val entity = drinkJpaRepository.findByIdOrNull(drinkId) ?: return null
        return toDomain(entity)
    }

    override fun findAllModifiers(): List<Modifier> =
        modifierJpaRepository.findAllByOrderById().map { toDomain(it) }

    override fun findByIds(ingredientIds: Collection<Long>): List<Ingredient> =
        ingredientJpaRepository.findAllById(ingredientIds).map { Ingredient(it.id, it.code, it.name, it.unit) }

    private fun toDomain(entity: ModifierEntity): Modifier =
        Modifier(
            id = entity.id,
            code = entity.code,
            name = entity.name,
            category = entity.category,
            price = Money(entity.priceRubles),
            ingredientId = entity.ingredientId,
            quantityPerUnit = entity.quantityPerUnit,
        )

    private fun toDomain(entity: BaseDrinkEntity): BaseDrink {
        val sizes = entity.sizes
            .map { DrinkSizeOption(it.size, Money(it.priceRubles), it.volumeMl, it.recipeScalePercent) }
            .sortedBy { it.size }
        return BaseDrink(
            id = entity.id,
            code = entity.code,
            name = entity.name,
            description = entity.description,
            milkPortion = entity.milkPortion,
            defaultMilkModifierId = entity.defaultMilkModifierId,
            sizes = sizes,
            temperatures = entity.temperatures.toSet(),
            recipe = entity.recipe.map { RecipeItem(it.ingredientId, it.quantity) },
            modifierLimits = entity.modifierLimits.map { ModifierLimit(it.category, it.minCount, it.maxCount) },
        )
    }
}
