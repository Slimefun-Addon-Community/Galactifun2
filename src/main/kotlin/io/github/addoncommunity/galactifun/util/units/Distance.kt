package io.github.addoncommunity.galactifun.util.units

import io.github.addoncommunity.galactifun.util.Constants
import kotlin.math.abs

@JvmInline
value class Distance private constructor(val lightYears: Double) : Comparable<Distance> {

    val kilometers: Double
        get() = lightYears * Constants.KM_PER_LY

    companion object {
        val Double.lightYears: Distance
            get() = Distance(this)

        val Int.lightYears: Distance
            get() = Distance(this.toDouble())

        val Long.lightYears: Distance
            get() = Distance(this.toDouble())

        val Double.kilometers: Distance
            get() = Distance(this / Constants.KM_PER_LY)

        val Long.kilometers: Distance
            get() = Distance(this / Constants.KM_PER_LY)

        val Int.kilometers: Distance
            get() = Distance(this / Constants.KM_PER_LY)

        val Double.au: Distance
            get() = Distance(this * Constants.AU_PER_LY)

        val Int.au: Distance
            get() = Distance(this * Constants.AU_PER_LY)

        val Long.au: Distance
            get() = Distance(this * Constants.AU_PER_LY)
    }

    init {
        require(lightYears >= 0) { "Distance must be positive" }
    }

    operator fun plus(other: Distance): Distance {
        return Distance(lightYears + other.lightYears)
    }

    operator fun minus(other: Distance): Distance {
        return Distance(abs(lightYears - other.lightYears))
    }

    operator fun times(other: Double): Distance {
        return Distance(lightYears * other)
    }

    operator fun times(other: Int): Distance {
        return Distance(lightYears * other)
    }

    operator fun times(other: Long): Distance {
        return Distance(lightYears * other)
    }

    operator fun div(other: Double): Distance {
        return Distance(lightYears / other)
    }

    operator fun div(other: Int): Distance {
        return Distance(lightYears / other)
    }

    operator fun div(other: Long): Distance {
        return Distance(lightYears / other)
    }

    override fun compareTo(other: Distance): Int {
        return lightYears.compareTo(other.lightYears)
    }
}