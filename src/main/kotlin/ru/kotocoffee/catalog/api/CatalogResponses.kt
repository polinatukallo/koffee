package ru.kotocoffee.catalog.api

import io.swagger.v3.oas.annotations.media.Schema
import ru.kotocoffee.catalog.domain.MenuItem
import ru.kotocoffee.shared.kernel.DrinkSize
import ru.kotocoffee.shared.kernel.ModifierCategory
import ru.kotocoffee.shared.kernel.Temperature
import java.math.BigDecimal

@Schema(description = "Базовый напиток в меню с правилами настройки")
data class MenuDrinkResponse(
    @field:Schema(example = "3")
    val id: Long,
    @field:Schema(example = "cappuccino")
    val code: String,
    @field:Schema(example = "Капучино")
    val name: String,
    val description: String,
    @field:Schema(description = "Цена самого маленького размера, ₽", example = "220")
    val startingPriceRubles: BigDecimal,
    @field:Schema(description = "Доступные размеры")
    val sizes: List<SizeResponse>,
    @field:Schema(description = "Доступные температуры подачи")
    val temperatures: List<Temperature>,
    @field:Schema(description = "Лимиты модификаторов по категориям. Категории, которых нет в списке, недоступны")
    val modifierLimits: List<ModifierLimitResponse>,
    @field:Schema(description = "Модификаторы, которые можно добавить к напитку")
    val modifiers: List<ModifierResponse>,
) {
    companion object {
        fun from(item: MenuItem): MenuDrinkResponse {
            val drink = item.drink
            return MenuDrinkResponse(
                id = drink.id,
                code = drink.code,
                name = drink.name,
                description = drink.description,
                startingPriceRubles = drink.startingPrice().rubles,
                sizes = drink.sizes.map { SizeResponse(it.size, it.price.rubles, it.volumeMl) },
                temperatures = drink.temperatures.sorted(),
                modifierLimits = drink.modifierLimits.map { ModifierLimitResponse(it.category, it.minCount, it.maxCount) },
                modifiers = item.allowedModifiers.map { ModifierResponse(it.id, it.code, it.name, it.category, it.price.rubles) },
            )
        }
    }
}

@Schema(description = "Размер напитка")
data class SizeResponse(
    @field:Schema(example = "M")
    val size: DrinkSize,
    @field:Schema(description = "Цена, ₽", example = "260")
    val priceRubles: BigDecimal,
    @field:Schema(description = "Объём, мл", example = "350")
    val volumeMl: Int,
)

@Schema(description = "Сколько модификаторов категории можно выбрать")
data class ModifierLimitResponse(
    @field:Schema(example = "SYRUP")
    val category: ModifierCategory,
    @field:Schema(example = "0")
    val minCount: Int,
    @field:Schema(example = "3")
    val maxCount: Int,
)

@Schema(description = "Модификатор напитка")
data class ModifierResponse(
    @field:Schema(example = "6")
    val id: Long,
    @field:Schema(example = "syrup_vanilla")
    val code: String,
    @field:Schema(example = "Сироп ваниль")
    val name: String,
    @field:Schema(example = "SYRUP")
    val category: ModifierCategory,
    @field:Schema(description = "Цена одной порции, ₽", example = "40")
    val priceRubles: BigDecimal,
)
