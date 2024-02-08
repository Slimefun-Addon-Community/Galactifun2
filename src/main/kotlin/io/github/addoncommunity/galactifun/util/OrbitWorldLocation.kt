package io.github.addoncommunity.galactifun.util

import io.github.addoncommunity.galactifun.core.managers.WorldManager
import org.bukkit.Location

data class OrbitWorldLocation(val x: Int, val z: Int) {

    companion object {

        private const val ORBIT_SIZE = 267857

        fun fromLocation(x: Int, z: Int) = OrbitWorldLocation(x.floorDiv(ORBIT_SIZE), z.floorDiv(ORBIT_SIZE))

        fun fromLocation(location: Location) = fromLocation(location.blockX, location.blockZ)
    }

    val centerLocation: Location
        get() = Location(
            WorldManager.spaceWorld,
            x * ORBIT_SIZE + ORBIT_SIZE / 2.0,
            0.0,
            z * ORBIT_SIZE + ORBIT_SIZE / 2.0
        )
}
