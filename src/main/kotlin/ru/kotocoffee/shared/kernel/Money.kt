package ru.kotocoffee.shared.kernel

import java.math.BigDecimal
import java.math.RoundingMode

class Money(rubles: BigDecimal) : Comparable<Money> {

    val rubles: BigDecimal = rubles.setScale(SCALE, RoundingMode.UNNECESSARY)

    constructor(rubles: Long) : this(BigDecimal.valueOf(rubles))

    init {
        require(this.rubles.signum() >= 0) { "Сумма не может быть отрицательной: $rubles" }
    }

    operator fun plus(other: Money): Money = Money(rubles + other.rubles)

    operator fun times(multiplier: Int): Money = Money(rubles * BigDecimal.valueOf(multiplier.toLong()))

    override fun compareTo(other: Money): Int = rubles.compareTo(other.rubles)

    override fun equals(other: Any?): Boolean = other is Money && rubles == other.rubles

    override fun hashCode(): Int = rubles.hashCode()

    override fun toString(): String = "${rubles.toPlainString()} ₽"

    companion object {
        private const val SCALE = 2

        val ZERO = Money(0)
    }
}
