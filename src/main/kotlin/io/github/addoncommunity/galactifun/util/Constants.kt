package io.github.addoncommunity.galactifun.util

import org.bukkit.Location
import org.bukkit.World

object Constants {

    const val KM_PER_LY = 9.461e12
    const val KM_PER_AU = 1.496e8

    const val GRAVITATIONAL_CONSTANT = 6.674e-11
    const val EARTH_GRAVITY = 9.81

    const val MAX_RADIX = 36

    fun locationZero(world: World?): Location {
        return Location(world, 0.0, 0.0, 0.0)
    }
}