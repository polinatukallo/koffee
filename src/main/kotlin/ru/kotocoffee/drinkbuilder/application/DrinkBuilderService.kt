package ru.kotocoffee.drinkbuilder.application

import org.springframework.stereotype.Service
import ru.kotocoffee.drinkbuilder.domain.BuiltDrink
import ru.kotocoffee.drinkbuilder.domain.DrinkBuilder
import ru.kotocoffee.drinkbuilder.domain.DrinkConfiguration
import ru.kotocoffee.shared.kernel.MeasureUnit

@Service
class DrinkBuilderService(private val catalogPort: DrinkCatalogPort) {

    private val drinkBuilder = DrinkBuilder()

    fun build(configuration: DrinkConfiguration): BuiltDrink {
        val template = catalogPort.getTemplate(configuration.drinkId)
        return drinkBuilder.build(template, configuration)
    }

    fun preview(configuration: DrinkConfiguration): DrinkPreview {
        val drink = build(configuration)
        val ingredientIds = drink.composition.map { it.ingredientId }
        val labels = catalogPort.describeIngredients(ingredientIds)
        val composition = mutableListOf<CompositionLine>()
        for (portion in drink.composition) {
            val label = labels[portion.ingredientId]
            val name = label?.name ?: "Ингредиент #${portion.ingredientId}"
            val unit = label?.unit ?: MeasureUnit.PCS
            composition.add(CompositionLine(portion.ingredientId, name, unit, portion.quantity))
        }
        return DrinkPreview(drink, composition)
    }
}

data class DrinkPreview(
    val drink: BuiltDrink,
    val composition: List<CompositionLine>,
)

data class CompositionLine(
    val ingredientId: Long,
    val name: String,
    val unit: MeasureUnit,
    val quantity: Int,
)
