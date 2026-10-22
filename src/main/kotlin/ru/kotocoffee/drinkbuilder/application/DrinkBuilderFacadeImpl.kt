package ru.kotocoffee.drinkbuilder.application

import org.springframework.stereotype.Component
import ru.kotocoffee.drinkbuilder.DrinkBuildRequest
import ru.kotocoffee.drinkbuilder.DrinkBuilderFacade
import ru.kotocoffee.drinkbuilder.PricedDrink
import ru.kotocoffee.drinkbuilder.PricedModifier
import ru.kotocoffee.drinkbuilder.domain.DrinkConfiguration
import ru.kotocoffee.drinkbuilder.domain.ModifierChoice

@Component
class DrinkBuilderFacadeImpl(private val drinkBuilderService: DrinkBuilderService) : DrinkBuilderFacade {

    override fun build(request: DrinkBuildRequest): PricedDrink {
        val configuration = DrinkConfiguration(
            drinkId = request.drinkId,
            size = request.size,
            temperature = request.temperature,
            modifiers = request.modifiers.map { ModifierChoice(it.modifierId, it.quantity) },
        )
        val drink = drinkBuilderService.build(configuration)
        return PricedDrink(
            drinkId = drink.drinkId,
            drinkName = drink.drinkName,
            size = drink.size,
            temperature = drink.temperature,
            volumeMl = drink.volumeMl,
            unitPrice = drink.unitPrice,
            modifiers = drink.modifiers.map { PricedModifier(it.modifierId, it.name, it.category, it.quantity, it.unitPrice) },
        )
    }
}
