package ru.kotocoffee.drinkbuilder.domain

import ru.kotocoffee.shared.error.BusinessRuleViolationException

class DrinkConfigurationException(
    val violations: List<String>,
) : BusinessRuleViolationException(
    "Напиток нельзя собрать: " + violations.joinToString("; "),
    "invalid-drink-configuration",
    mapOf("violations" to violations),
)
