package ru.kotocoffee.drinkbuilder

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.kotocoffee.drinkbuilder.DrinkTemplates.BEANS
import ru.kotocoffee.drinkbuilder.DrinkTemplates.CARAMEL
import ru.kotocoffee.drinkbuilder.DrinkTemplates.CINNAMON
import ru.kotocoffee.drinkbuilder.DrinkTemplates.COW_MILK
import ru.kotocoffee.drinkbuilder.DrinkTemplates.COW_MILK_INGREDIENT
import ru.kotocoffee.drinkbuilder.DrinkTemplates.EXTRA_SHOT
import ru.kotocoffee.drinkbuilder.DrinkTemplates.HAZELNUT
import ru.kotocoffee.drinkbuilder.DrinkTemplates.OAT_MILK
import ru.kotocoffee.drinkbuilder.DrinkTemplates.OAT_MILK_INGREDIENT
import ru.kotocoffee.drinkbuilder.DrinkTemplates.VANILLA
import ru.kotocoffee.drinkbuilder.DrinkTemplates.VANILLA_INGREDIENT
import ru.kotocoffee.drinkbuilder.domain.DrinkBuilder
import ru.kotocoffee.drinkbuilder.domain.DrinkConfiguration
import ru.kotocoffee.drinkbuilder.domain.DrinkConfigurationException
import ru.kotocoffee.drinkbuilder.domain.IngredientPortion
import ru.kotocoffee.drinkbuilder.domain.ModifierChoice
import ru.kotocoffee.shared.kernel.DrinkSize
import ru.kotocoffee.shared.kernel.Money
import ru.kotocoffee.shared.kernel.Temperature

class DrinkBuilderTest {

    private val builder = DrinkBuilder()

    @Test
    fun `builds the example drink from the product description`() {
        val configuration = DrinkConfiguration(
            drinkId = 3,
            size = DrinkSize.L,
            temperature = Temperature.ICED,
            modifiers = listOf(
                ModifierChoice(OAT_MILK.id, 1),
                ModifierChoice(CARAMEL.id, 1),
                ModifierChoice(VANILLA.id, 1),
                ModifierChoice(EXTRA_SHOT.id, 1),
            ),
        )

        val drink = builder.build(DrinkTemplates.cappuccino(), configuration)

        assertEquals(Money(490), drink.unitPrice)
        assertEquals(450, drink.volumeMl)
        assertEquals(Temperature.ICED, drink.temperature)
        assertEquals(4, drink.modifiers.size)
        assertEquals(
            listOf(
                IngredientPortion(BEANS, 41),
                IngredientPortion(OAT_MILK_INGREDIENT, 270),
                IngredientPortion(VANILLA_INGREDIENT, 15),
                IngredientPortion(DrinkTemplates.CARAMEL_INGREDIENT, 15),
            ),
            drink.composition,
        )
    }

    @Test
    fun `adds default milk when milk is required but not chosen`() {
        val configuration = DrinkConfiguration(3, DrinkSize.S, Temperature.HOT, emptyList())

        val drink = builder.build(DrinkTemplates.cappuccino(), configuration)

        assertEquals(Money(220), drink.unitPrice)
        assertEquals(COW_MILK.id, drink.modifiers.first().modifierId)
        assertTrue(drink.composition.contains(IngredientPortion(COW_MILK_INGREDIENT, 150)))
    }

    @Test
    fun `sums repeated choices of the same modifier`() {
        val configuration = DrinkConfiguration(
            3,
            DrinkSize.M,
            Temperature.HOT,
            listOf(ModifierChoice(VANILLA.id, 1), ModifierChoice(VANILLA.id, 1)),
        )

        val drink = builder.build(DrinkTemplates.cappuccino(), configuration)

        val vanilla = drink.modifiers.first { it.modifierId == VANILLA.id }
        assertEquals(2, vanilla.quantity)
        assertEquals(Money(260 + 80), drink.unitPrice)
        assertTrue(drink.composition.contains(IngredientPortion(VANILLA_INGREDIENT, 30)))
    }

    @Test
    fun `rejects more syrups than allowed by the drink`() {
        val configuration = DrinkConfiguration(
            3,
            DrinkSize.M,
            Temperature.HOT,
            listOf(ModifierChoice(VANILLA.id, 2), ModifierChoice(CARAMEL.id, 1), ModifierChoice(HAZELNUT.id, 1)),
        )

        val exception = assertThrows<DrinkConfigurationException> {
            builder.build(DrinkTemplates.cappuccino(), configuration)
        }

        assertEquals(1, exception.violations.size)
        assertTrue(exception.violations.first().contains("SYRUP"))
    }

    @Test
    fun `rejects two kinds of milk`() {
        val configuration = DrinkConfiguration(
            3,
            DrinkSize.S,
            Temperature.HOT,
            listOf(ModifierChoice(OAT_MILK.id, 1), ModifierChoice(COW_MILK.id, 1)),
        )

        assertThrows<DrinkConfigurationException> { builder.build(DrinkTemplates.cappuccino(), configuration) }
    }

    @Test
    fun `rejects modifier that is not allowed for the drink`() {
        val configuration = DrinkConfiguration(
            1,
            DrinkSize.S,
            Temperature.HOT,
            listOf(ModifierChoice(OAT_MILK.id, 1), ModifierChoice(CINNAMON.id, 1)),
        )

        val exception = assertThrows<DrinkConfigurationException> {
            builder.build(DrinkTemplates.espresso(), configuration)
        }

        assertEquals(2, exception.violations.size)
    }

    @Test
    fun `collects all violations at once`() {
        val configuration = DrinkConfiguration(
            1,
            DrinkSize.L,
            Temperature.ICED,
            listOf(ModifierChoice(EXTRA_SHOT.id, 2), ModifierChoice(VANILLA.id, 0)),
        )

        val exception = assertThrows<DrinkConfigurationException> {
            builder.build(DrinkTemplates.espresso(), configuration)
        }

        assertEquals(4, exception.violations.size)
    }

    @Test
    fun `espresso without modifiers costs the base price`() {
        val drink = builder.build(DrinkTemplates.espresso(), DrinkConfiguration(1, DrinkSize.S, Temperature.HOT, emptyList()))

        assertEquals(Money(150), drink.unitPrice)
        assertEquals(40, drink.volumeMl)
        assertTrue(drink.modifiers.isEmpty())
        assertEquals(listOf(IngredientPortion(BEANS, 18)), drink.composition)
    }
}
