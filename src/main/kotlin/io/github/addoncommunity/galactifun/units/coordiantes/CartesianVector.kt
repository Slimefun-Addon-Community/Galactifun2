package io.github.addoncommunity.galactifun.units.coordiantes

import io.github.addoncommunity.galactifun.units.Angle.Companion.radians
import io.github.addoncommunity.galactifun.units.Distance
import io.github.addoncommunity.galactifun.units.Distance.Companion.meters
import kotlin.math.atan2
import kotlin.math.sqrt

data class CartesianVector(
    val x: Distance,
    val y: Distance
) {

    val polar by lazy {
        val theta = atan2(y.meters, x.meters).radians
        PolarVector(length, theta)
    }

    val length by lazy {
        sqrt(x.meters * x.meters + y.meters * y.meters).meters
    }

    operator fun plus(other: CartesianVector): CartesianVector {
        return CartesianVector(x + other.x, y + other.y)
    }

    operator fun minus(other: CartesianVector): CartesianVector {
        return CartesianVector(x - other.x, y - other.y)
    }

    operator fun times(scalar: Double): CartesianVector {
        return CartesianVector(x * scalar, y * scalar)
    }

    operator fun div(scalar: Double): CartesianVector {
        return CartesianVector(x / scalar, y / scalar)
    }

    fun distanceTo(other: CartesianVector): Distance {
        val term1 = (x - other.x).meters
        val term2 = (y - other.y).meters
        return sqrt(term1 * term1 + term2 * term2).meters
    }
}