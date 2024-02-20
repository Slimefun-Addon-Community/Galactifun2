package io.github.addoncommunity.galactifun.units

import io.github.addoncommunity.galactifun.units.quantities.Inverse
import io.github.addoncommunity.galactifun.units.quantities.Quantity
import java.text.NumberFormat

@JvmInline
value class Measure<Q : Quantity> internal constructor(internal val value: Double) : Comparable<Measure<Q>> {
    override fun compareTo(other: Measure<Q>): Int {
        return value.compareTo(other.value)
    }

    operator fun plus(other: Measure<Q>): Measure<Q> {
        return Measure(value + other.value)
    }

    operator fun minus(other: Measure<Q>): Measure<Q> {
        return Measure(value - other.value)
    }

    operator fun times(scalar: Double): Measure<Q> {
        return Measure(value * scalar)
    }

    operator fun times(scalar: Int): Measure<Q> {
        return Measure(value * scalar)
    }

    operator fun div(scalar: Double): Measure<Q> {
        return Measure(value / scalar)
    }

    operator fun div(scalar: Int): Measure<Q> {
        return Measure(value / scalar)
    }

    operator fun unaryMinus(): Measure<Q> {
        return Measure(-value)
    }

    operator fun unaryPlus(): Measure<Q> {
        return this
    }

    fun inUnit(unit: Q): Double {
        return value / unit.ratio
    }

    fun toString(unit: Q, short: Boolean = false, formatter: NumberFormat = NumberFormat.getNumberInstance()): String {
        val value = inUnit(unit)
        val formatted = formatter.format(value)
        return buildString {
            append(formatted)
            append(' ')
            if (short) {
                append(unit.symbol)
            } else {
                append(unit.name)
                if (value != 1.0) append('s')
            }
        }
    }
}

operator fun <Q : Quantity> Double.times(quantity: Q): Measure<Q> = Measure(this * quantity.ratio)
operator fun <Q: Quantity> Int.times(quantity: Q): Measure<Q> = this.toDouble() * quantity
operator fun <Q : Quantity> Double.div(quantity: Q): Measure<Inverse<Q>> = Measure(this / quantity.ratio)
operator fun <Q : Quantity> Int.div(quantity: Q): Measure<Inverse<Q>> = Measure(this / quantity.ratio)
operator fun <Q : Quantity> Double.times(measure: Measure<Q>): Measure<Q> = measure * this
operator fun <Q : Quantity> Int.times(measure: Measure<Q>): Measure<Q> = this.toDouble() * measure
operator fun <Q : Quantity> Double.div(measure: Measure<Q>): Measure<Inverse<Q>> = Measure(this / measure.value)
operator fun <Q : Quantity> Int.div(measure: Measure<Q>): Measure<Inverse<Q>> = this.toDouble() / measure
operator fun <Q : Quantity> Double.div(measure: Measure<Inverse<Q>>): Measure<Q> = Measure(this * measure.value)
operator fun <Q : Quantity> Int.div(measure: Measure<Inverse<Q>>): Measure<Q> = this.toDouble() / measure