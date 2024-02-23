package io.github.addoncommunity.galactifun.units.coordiantes

import io.github.addoncommunity.galactifun.units.Angle
import io.github.addoncommunity.galactifun.units.Distance
import io.github.addoncommunity.galactifun.units.Distance.Companion.meters
import io.github.addoncommunity.galactifun.units.cos
import io.github.addoncommunity.galactifun.units.sin
import org.joml.Vector3d

data class SphericalVector(
    val rightAscension: Angle,
    val declination: Angle,
    val distance: Distance
) {

    private val cartesianPosition by lazy {
        val x = distance * sin(declination) * cos(rightAscension)
        val y = distance * sin(declination) * sin(rightAscension)
        val z = distance * cos(declination)
        Vector3d(x.meters, y.meters, z.meters)
    }

    // What? I'm not afraid of a few trigonometric functions and a square root or two
    fun distanceTo(other: SphericalVector): Distance {
        return cartesianPosition.distance(other.cartesianPosition).meters
    }
}