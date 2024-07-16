package io.github.addoncommunity.galactifun.api.objects.properties

import io.github.addoncommunity.galactifun.impl.managers.PlanetManager
import io.github.addoncommunity.galactifun.util.bukkit.copy
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
data class OrbitPosition(val x: Int, val z: Int) {

    companion object {

        const val ORBIT_SIZE = 535714

        fun fromLocation(x: Int, z: Int) = OrbitPosition(x.floorDiv(ORBIT_SIZE), z.floorDiv(ORBIT_SIZE))

        fun fromLocation(location: Location) = fromLocation(location.blockX, location.blockZ)
    }

    val centerLocation: Location
        get() = Location(
            PlanetManager.spaceWorld,
            x * ORBIT_SIZE + ORBIT_SIZE / 2.0,
            0.0,
            z * ORBIT_SIZE + ORBIT_SIZE / 2.0
        )

    fun offset(x: Double, y: Double, z: Double) = centerLocation.copy(
        x = centerLocation.x + x,
        y = centerLocation.y + y,
        z = centerLocation.z + z
    )
}
