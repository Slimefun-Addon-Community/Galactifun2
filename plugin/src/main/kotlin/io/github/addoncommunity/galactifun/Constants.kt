package io.github.addoncommunity.galactifun

import org.bukkit.Location
import org.bukkit.World

object Constants {

    const val KM_PER_LY = 9.461e12
    const val KM_PER_PC = 3.086e13
    const val KM_PER_AU =  1.495978707e8

    const val GRAVITATIONAL_CONSTANT = 6.674e-11
    const val EARTH_GRAVITY = 9.81

    /**
     * The maximum radix for the [Int.toString] and [String.toInt] functions.
     */
    const val MAX_RADIX = 36

    fun locationZero(world: World?): Location {
        return Location(world, 0.0, 0.0, 0.0)
    }
}