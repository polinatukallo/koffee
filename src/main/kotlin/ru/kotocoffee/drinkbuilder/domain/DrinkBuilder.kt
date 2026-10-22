package ru.kotocoffee.drinkbuilder.domain

import ru.kotocoffee.shared.kernel.ModifierCategory
import java.util.SortedMap

class DrinkBuilder {

    fun build(template: DrinkTemplate, configuration: DrinkConfiguration): BuiltDrink {
        val violations = mutableListOf<String>()

        val sizeVariant = template.findSize(configuration.size)
        if (sizeVariant == null) {
            violations.add("Размер ${configuration.size} недоступен для напитка «${template.name}»")
        }
        if (configuration.temperature !in template.temperatures) {
            violations.add("Температура ${configuration.temperature} недоступна для напитка «${template.name}»")
        }

        val selected = collectModifiers(template, configuration, violations)
        addDefaultMilkIfMissing(template, selected)
        checkCategoryLimits(template, selected, violations)

        if (violations.isNotEmpty() || sizeVariant == null) {
            throw DrinkConfigurationException(violations)
        }

        val appliedModifiers = mutableListOf<AppliedModifier>()
        var price = sizeVariant.price
        for (selection in selected) {
            val option = selection.option
            appliedModifiers.add(AppliedModifier(option.id, option.name, option.category, selection.quantity, option.price))
            price += option.price * selection.quantity
        }

        return BuiltDrink(
            drinkId = template.drinkId,
            drinkName = template.name,
            size = sizeVariant.size,
            temperature = configuration.temperature,
            volumeMl = sizeVariant.volumeMl,
            unitPrice = price,
            modifiers = appliedModifiers,
            composition = composition(template, sizeVariant, selected),
        )
    }

    private fun collectModifiers(
        template: DrinkTemplate,
        configuration: DrinkConfiguration,
        violations: MutableList<String>,
    ): MutableList<SelectedModifier> {
        val selected = mutableListOf<SelectedModifier>()
        for (choice in configuration.modifiers) {
            if (choice.quantity < 1) {
                violations.add("Количество модификатора ${choice.modifierId} должно быть не меньше 1")
                continue
            }
            val option = template.findModifier(choice.modifierId)
            if (option == null) {
                violations.add("Модификатор ${choice.modifierId} недоступен для напитка «${template.name}»")
                continue
            }
            val existing = findSelected(selected, option.id)
            if (existing == null) {
                selected.add(SelectedModifier(option, choice.quantity))
            } else {
                existing.quantity += choice.quantity
            }
        }
        return selected
    }

    private fun addDefaultMilkIfMissing(template: DrinkTemplate, selected: MutableList<SelectedModifier>) {
        val milkLimit = template.findLimit(ModifierCategory.MILK) ?: return
        if (milkLimit.minCount < 1 || countInCategory(selected, ModifierCategory.MILK) > 0) {
            return
        }
        val defaultMilkId = template.defaultMilkModifierId ?: return
        val defaultMilk = template.findModifier(defaultMilkId) ?: return
        selected.add(0, SelectedModifier(defaultMilk, 1))
    }

    private fun checkCategoryLimits(
        template: DrinkTemplate,
        selected: List<SelectedModifier>,
        violations: MutableList<String>,
    ) {
        for (category in ModifierCategory.entries) {
            val count = countInCategory(selected, category)
            val limit = template.findLimit(category)
            if (limit == null) {
                if (count > 0) {
                    violations.add("Модификаторы категории $category недоступны для напитка «${template.name}»")
                }
                continue
            }
            if (count < limit.minCount) {
                violations.add("Для напитка «${template.name}» нужно выбрать не меньше ${limit.minCount} в категории $category")
            }
            if (count > limit.maxCount) {
                violations.add(
                    "Для напитка «${template.name}» в категории $category можно выбрать не больше ${limit.maxCount}, выбрано $count",
                )
            }
        }
    }

    private fun composition(
        template: DrinkTemplate,
        sizeVariant: SizeVariant,
        selected: List<SelectedModifier>,
    ): List<IngredientPortion> {
        val totals: SortedMap<Long, Int> = sortedMapOf()
        for (portion in template.recipe) {
            addQuantity(totals, portion.ingredientId, sizeVariant.scale(portion.quantity))
        }
        for (selection in selected) {
            val option = selection.option
            val quantity = if (option.category == ModifierCategory.MILK) {
                sizeVariant.scale(template.milkPortion) * selection.quantity
            } else {
                option.quantityPerUnit * selection.quantity
            }
            addQuantity(totals, option.ingredientId, quantity)
        }
        val result = mutableListOf<IngredientPortion>()
        for ((ingredientId, quantity) in totals) {
            if (quantity > 0) {
                result.add(IngredientPortion(ingredientId, quantity))
            }
        }
        return result
    }

    private fun addQuantity(totals: MutableMap<Long, Int>, ingredientId: Long, quantity: Int) {
        val current = totals[ingredientId] ?: 0
        totals[ingredientId] = current + quantity
    }

    private fun countInCategory(selected: List<SelectedModifier>, category: ModifierCategory): Int {
        var count = 0
        for (selection in selected) {
            if (selection.option.category == category) {
                count += selection.quantity
            }
        }
        return count
    }

    private fun findSelected(selected: List<SelectedModifier>, modifierId: Long): SelectedModifier? {
        for (selection in selected) {
            if (selection.option.id == modifierId) {
                return selection
            }
        }
        return null
    }

    private class SelectedModifier(val option: ModifierOption, var quantity: Int)
}

