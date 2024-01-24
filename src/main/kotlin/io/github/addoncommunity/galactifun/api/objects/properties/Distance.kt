package io.github.addoncommunity.galactifun.api.objects.properties

import io.github.addoncommunity.galactifun.util.Constants
import kotlin.math.abs

@JvmInline
value class Distance private constructor(val lightYears: Double) {

    val kilometers: Long
        get() = (lightYears * Constants.KM_PER_LY).toLong()

    companion object {

        @get:JvmName("lightYears")
        val Double.lightYears: Distance
            get() = Distance(this)

        @get:JvmName("lightYears")
        val Int.lightYears: Distance
            get() = Distance(this.toDouble())

        @get:JvmName("lightYears")
        val Long.lightYears: Distance
            get() = Distance(this.toDouble())

        @get:JvmName("kilometers")
        val Long.kilometers: Distance
            get() = Distance(this / Constants.KM_PER_LY)

        @get:JvmName("kilometers")
        val Int.kilometers: Distance
            get() = Distance(this / Constants.KM_PER_LY)
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
}