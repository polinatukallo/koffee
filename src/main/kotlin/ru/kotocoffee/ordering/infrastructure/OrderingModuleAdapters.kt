package ru.kotocoffee.ordering.infrastructure

import org.springframework.stereotype.Component
import ru.kotocoffee.drinkbuilder.DrinkBuildRequest
import ru.kotocoffee.drinkbuilder.DrinkBuilderFacade
import ru.kotocoffee.drinkbuilder.ModifierSelection
import ru.kotocoffee.ordering.application.DrinkPricingPort
import ru.kotocoffee.ordering.application.PricedDrinkSpec
import ru.kotocoffee.ordering.domain.DrinkSpec
import ru.kotocoffee.ordering.domain.OrderLineModifier

@Component
class DrinkBuilderPricingAdapter(private val drinkBuilderFacade: DrinkBuilderFacade) : DrinkPricingPort {

    override fun price(spec: DrinkSpec): PricedDrinkSpec {
        val request = DrinkBuildRequest(
            drinkId = spec.drinkId,
            size = spec.size,
            temperature = spec.temperature,
            modifiers = spec.modifiers.map { ModifierSelection(it.modifierId, it.quantity) },
        )
        val drink = drinkBuilderFacade.build(request)
        return PricedDrinkSpec(
            drinkId = drink.drinkId,
            drinkName = drink.drinkName,
            size = drink.size,
            temperature = drink.temperature,
            volumeMl = drink.volumeMl,
            unitPrice = drink.unitPrice,
            modifiers = drink.modifiers.map { OrderLineModifier(it.modifierId, it.name, it.category, it.quantity, it.unitPrice) },
        )
    }
}
