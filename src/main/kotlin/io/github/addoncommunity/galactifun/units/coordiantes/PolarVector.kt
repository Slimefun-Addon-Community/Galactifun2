package io.github.addoncommunity.galactifun.units.coordiantes

import io.github.addoncommunity.galactifun.units.Angle
import io.github.addoncommunity.galactifun.units.Distance
import io.github.addoncommunity.galactifun.units.cos
import io.github.addoncommunity.galactifun.units.sin

data class PolarVector(
    val radius: Distance,
    val angle: Angle
) {

    val cartesian: CartesianVector by lazy {
        CartesianVector(
            x = radius * cos(angle),
            y = radius * sin(angle)
        )
    }

    operator fun times(scalar: Double): PolarVector {
        return PolarVector(radius * scalar, angle * scalar)
    }

    operator fun div(scalar: Double): PolarVector {
        return PolarVector(radius / scalar, angle / scalar)
    }

    fun distanceTo(other: PolarVector): Distance {
        return cartesian.distanceTo(other.cartesian)
    }
}