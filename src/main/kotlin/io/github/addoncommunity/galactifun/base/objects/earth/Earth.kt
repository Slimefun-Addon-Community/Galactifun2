package io.github.addoncommunity.galactifun.base.objects.earth

import io.github.addoncommunity.galactifun.api.objects.planet.PlanetaryWorld
import io.github.addoncommunity.galactifun.api.objects.properties.DayCycle
import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Atmosphere
import io.github.addoncommunity.galactifun.base.BaseUniverse
import io.github.addoncommunity.galactifun.pluginInstance
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.inventory.ItemStack

class Earth : PlanetaryWorld("Earth", ItemStack(Material.GRASS_BLOCK)) {

    override val orbiting = BaseUniverse.solarSystem
    override val orbit = Orbit.kilometers(149600000L, years = 1.0)
    override val dayCycle = DayCycle.EARTH_LIKE
    override val atmosphere = Atmosphere.EARTH_LIKE

    override fun loadWorld(): World {
        val name = pluginInstance.config.getString("worlds.earth") ?: "world"
        return WorldCreator(name).createWorld() // load the world
            ?: error("Failed to read earth world name from config; no default world found!")
    }
}