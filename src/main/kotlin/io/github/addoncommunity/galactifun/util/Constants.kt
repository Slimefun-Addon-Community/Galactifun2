package io.github.addoncommunity.galactifun.util

import org.bukkit.Location
import org.bukkit.World

object Constants {

    const val KM_PER_LY = 9.461e12
    const val AU_PER_LY = 63241.1

    fun locationZero(world: World?): Location {
        return Location(world, 0.0, 0.0, 0.0)
    }
}