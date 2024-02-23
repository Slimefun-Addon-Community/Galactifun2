package io.github.addoncommunity.galactifun.units

import io.github.addoncommunity.galactifun.units.Distance.Companion.meters
import kotlin.math.sqrt

data class SphericalPosition(
    val rightAscension: Angle,
    val declination: Angle,
    val distance: Distance
) {

    // What? I'm not afraid of a few trigonometric functions and a square root or two
    fun distanceTo(other: SphericalPosition): Distance {
        val x = distance * sin(declination) * cos(rightAscension)
        val y = distance * sin(declination) * sin(rightAscension)
        val z = distance * cos(declination)

        val x2 = other.distance * sin(other.declination) * cos(other.rightAscension)
        val y2 = other.distance * sin(other.declination) * sin(other.rightAscension)
        val z2 = other.distance * cos(other.declination)

        val term1 = (x - x2).meters
        val term2 = (y - y2).meters
        val term3 = (z - z2).meters
        return sqrt(term1 * term1 + term2 * term2 + term3 * term3).meters
    }
}