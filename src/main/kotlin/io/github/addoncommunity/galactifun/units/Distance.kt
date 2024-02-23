package io.github.addoncommunity.galactifun.units

import io.github.addoncommunity.galactifun.util.Constants
import kotlin.math.abs

@JvmInline
value class Distance private constructor(val meters: Double) : Comparable<Distance> {

    val lightYears: Double
        get() = kilometers / Constants.KM_PER_LY

    val kilometers: Double
        get() = meters / 1000

    companion object {
        val Double.lightYears: Distance
            get() = (this * Constants.KM_PER_LY).kilometers

        val Double.kilometers: Distance
            get() = (this * 1000).meters

        val Double.meters: Distance
            get() = Distance(this)

        val Double.au: Distance
            get() = (this * Constants.KM_PER_AU).kilometers

        fun fromParallax(parallax: Angle): Distance {
            return ((1 / parallax.arcseconds) * Constants.KM_PER_PC).kilometers
        }
    }

    init {
        require(meters >= 0) { "Distance must be positive" }
    }

    operator fun plus(other: Distance): Distance = Distance(meters + other.meters)
    operator fun minus(other: Distance): Distance = Distance(abs(meters - other.meters))
    operator fun times(scalar: Double): Distance = Distance(meters * scalar)
    operator fun div(scalar: Double): Distance = Distance(meters / scalar)
    operator fun rem(scalar: Double): Distance = Distance(meters % scalar)
    operator fun unaryMinus() = Distance(-meters)
    operator fun unaryPlus() = this

    override fun compareTo(other: Distance): Int = meters.compareTo(other.meters)
}