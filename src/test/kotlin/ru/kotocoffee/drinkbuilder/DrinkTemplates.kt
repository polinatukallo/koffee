package ru.kotocoffee.drinkbuilder

import ru.kotocoffee.drinkbuilder.domain.CategoryLimit
import ru.kotocoffee.drinkbuilder.domain.DrinkTemplate
import ru.kotocoffee.drinkbuilder.domain.IngredientPortion
import ru.kotocoffee.drinkbuilder.domain.ModifierOption
import ru.kotocoffee.drinkbuilder.domain.SizeVariant
import ru.kotocoffee.shared.kernel.DrinkSize
import ru.kotocoffee.shared.kernel.ModifierCategory
import ru.kotocoffee.shared.kernel.Money
import ru.kotocoffee.shared.kernel.Temperature

object DrinkTemplates {
    const val BEANS = 1L
    const val COW_MILK_INGREDIENT = 3L
    const val OAT_MILK_INGREDIENT = 4L
    const val VANILLA_INGREDIENT = 7L
    const val CARAMEL_INGREDIENT = 8L
    const val HAZELNUT_INGREDIENT = 9L
    const val CINNAMON_INGREDIENT = 10L

    val COW_MILK = ModifierOption(1, "Коровье молоко", ModifierCategory.MILK, Money(0), COW_MILK_INGREDIENT, 0)
    val OAT_MILK = ModifierOption(2, "Овсяное молоко", ModifierCategory.MILK, Money(60), OAT_MILK_INGREDIENT, 0)
    val VANILLA = ModifierOption(5, "Сироп ваниль", ModifierCategory.SYRUP, Money(40), VANILLA_INGREDIENT, 15)
    val CARAMEL = ModifierOption(6, "Сироп солёная карамель", ModifierCategory.SYRUP, Money(40), CARAMEL_INGREDIENT, 15)
    val HAZELNUT = ModifierOption(7, "Сироп лесной орех", ModifierCategory.SYRUP, Money(40), HAZELNUT_INGREDIENT, 15)
    val EXTRA_SHOT = ModifierOption(8, "Доп. шот", ModifierCategory.EXTRA_SHOT, Money(50), BEANS, 9)
    val CINNAMON = ModifierOption(9, "Корица", ModifierCategory.TOPPING, Money(15), CINNAMON_INGREDIENT, 1)

    fun cappuccino(): DrinkTemplate =
        DrinkTemplate(
            drinkId = 3,
            name = "Капучино",
            milkPortion = 150,
            defaultMilkModifierId = COW_MILK.id,
            sizes = listOf(
                SizeVariant(DrinkSize.S, Money(220), 250, 100),
                SizeVariant(DrinkSize.M, Money(260), 350, 140),
                SizeVariant(DrinkSize.L, Money(300), 450, 180),
            ),
            temperatures = setOf(Temperature.HOT, Temperature.ICED),
            recipe = listOf(IngredientPortion(BEANS, 18)),
            limits = listOf(
                CategoryLimit(ModifierCategory.MILK, 1, 1),
                CategoryLimit(ModifierCategory.SYRUP, 0, 3),
                CategoryLimit(ModifierCategory.EXTRA_SHOT, 0, 2),
                CategoryLimit(ModifierCategory.TOPPING, 0, 2),
            ),
            modifiers = listOf(COW_MILK, OAT_MILK, VANILLA, CARAMEL, HAZELNUT, EXTRA_SHOT, CINNAMON),
        )

    fun espresso(): DrinkTemplate =
        DrinkTemplate(
            drinkId = 1,
            name = "Эспрессо",
            milkPortion = 0,
            defaultMilkModifierId = null,
            sizes = listOf(SizeVariant(DrinkSize.S, Money(150), 40, 100)),
            temperatures = setOf(Temperature.HOT),
            recipe = listOf(IngredientPortion(BEANS, 18)),
            limits = listOf(
                CategoryLimit(ModifierCategory.SYRUP, 0, 1),
                CategoryLimit(ModifierCategory.EXTRA_SHOT, 0, 1),
            ),
            modifiers = listOf(VANILLA, CARAMEL, EXTRA_SHOT),
        )
}
