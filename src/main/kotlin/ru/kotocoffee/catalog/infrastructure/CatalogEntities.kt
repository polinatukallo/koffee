package ru.kotocoffee.catalog.infrastructure

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import ru.kotocoffee.shared.kernel.DrinkSize
import ru.kotocoffee.shared.kernel.MeasureUnit
import ru.kotocoffee.shared.kernel.ModifierCategory
import ru.kotocoffee.shared.kernel.Temperature
import java.math.BigDecimal

@Entity
@Table(name = "ingredients")
class IngredientEntity(
    @Id
    var id: Long,
    var code: String,
    var name: String,
    @Enumerated(EnumType.STRING)
    var unit: MeasureUnit,
)

@Entity
@Table(name = "modifiers")
class ModifierEntity(
    @Id
    var id: Long,
    var code: String,
    var name: String,
    @Enumerated(EnumType.STRING)
    var category: ModifierCategory,
    @Column(name = "price_rubles")
    var priceRubles: BigDecimal,
    @Column(name = "ingredient_id")
    var ingredientId: Long,
    @Column(name = "quantity_per_unit")
    var quantityPerUnit: Int,
)

@Entity
@Table(name = "base_drinks")
class BaseDrinkEntity(
    @Id
    var id: Long,
    var code: String,
    var name: String,
    var description: String,
    @Column(name = "milk_portion")
    var milkPortion: Int,
    @Column(name = "default_milk_modifier_id")
    var defaultMilkModifierId: Long?,
    @ElementCollection
    @CollectionTable(name = "base_drink_sizes", joinColumns = [JoinColumn(name = "base_drink_id")])
    var sizes: MutableList<DrinkSizeEmbeddable> = mutableListOf(),
    @ElementCollection
    @CollectionTable(name = "base_drink_temperatures", joinColumns = [JoinColumn(name = "base_drink_id")])
    @Column(name = "temperature")
    @Enumerated(EnumType.STRING)
    var temperatures: MutableSet<Temperature> = mutableSetOf(),
    @ElementCollection
    @CollectionTable(name = "base_drink_ingredients", joinColumns = [JoinColumn(name = "base_drink_id")])
    var recipe: MutableList<RecipeItemEmbeddable> = mutableListOf(),
    @ElementCollection
    @CollectionTable(name = "base_drink_modifier_limits", joinColumns = [JoinColumn(name = "base_drink_id")])
    var modifierLimits: MutableList<ModifierLimitEmbeddable> = mutableListOf(),
)

@Embeddable
class DrinkSizeEmbeddable(
    @Enumerated(EnumType.STRING)
    var size: DrinkSize,
    @Column(name = "price_rubles")
    var priceRubles: BigDecimal,
    @Column(name = "volume_ml")
    var volumeMl: Int,
    @Column(name = "recipe_scale_percent")
    var recipeScalePercent: Int,
)

@Embeddable
class RecipeItemEmbeddable(
    @Column(name = "ingredient_id")
    var ingredientId: Long,
    var quantity: Int,
)

@Embeddable
class ModifierLimitEmbeddable(
    @Enumerated(EnumType.STRING)
    var category: ModifierCategory,
    @Column(name = "min_count")
    var minCount: Int,
    @Column(name = "max_count")
    var maxCount: Int,
)
