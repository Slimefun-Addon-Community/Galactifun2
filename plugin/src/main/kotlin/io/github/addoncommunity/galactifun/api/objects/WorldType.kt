package io.github.addoncommunity.galactifun.api.objects

import io.github.addoncommunity.galactifun.api.objects.planet.PlanetaryWorld
import io.github.addoncommunity.galactifun.impl.managers.PlanetManager
import org.bukkit.Location

sealed interface WorldType {

    data class Planet(val planet: PlanetaryWorld) : WorldType

    data class Space(val orbiting: PlanetaryObject) : WorldType

    companion object {
        fun fromLocation(location: Location): WorldType? {
            val world = location.world
            val planet = PlanetManager.getByWorld(world)
            if (planet != null) {
                return Planet(planet)
            }
            if (world == PlanetManager.spaceWorld) {
                return Space(PlanetManager.getOrbiting(location)!!)
            }
            return null
        }
    }
}