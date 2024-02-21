package io.github.addoncommunity.galactifun.util.units

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
            get() = Distance(this * Constants.KM_PER_LY * 1000)

        val Int.lightYears: Distance
            get() = this.toDouble().lightYears

        val Long.lightYears: Distance
            get() = this.toDouble().lightYears

        val Double.kilometers: Distance
            get() = Distance(this * 1000)

        val Long.kilometers: Distance
            get() = this.toDouble().kilometers

        val Int.kilometers: Distance
            get() = this.toDouble().kilometers

        val Double.meters: Distance
            get() = Distance(this)

        val Long.meters: Distance
            get() = this.toDouble().meters

        val Int.meters: Distance
            get() = this.toDouble().meters

        val Double.au: Distance
            get() = Distance(this * Constants.KM_PER_AU * 1000)

        val Int.au: Distance
            get() = this.toDouble().au

        val Long.au: Distance
            get() = this.toDouble().au
    }

    init {
        require(meters >= 0) { "Distance must be positive" }
    }

    operator fun plus(other: Distance): Distance {
        return Distance(meters + other.meters)
    }

    operator fun minus(other: Distance): Distance {
        return Distance(abs(meters - other.meters))
    }

    operator fun times(other: Double): Distance {
        return Distance(meters * other)
    }

    operator fun times(other: Int): Distance {
        return Distance(meters * other)
    }

    operator fun times(other: Long): Distance {
        return Distance(meters * other)
    }

    operator fun div(other: Double): Distance {
        return Distance(meters / other)
    }

    operator fun div(other: Int): Distance {
        return Distance(meters / other)
    }

    operator fun div(other: Long): Distance {
        return Distance(meters / other)
    }

    override fun compareTo(other: Distance): Int {
        return meters.compareTo(other.meters)
    }
}