package io.github.addoncommunity.galactifun.api.objects

import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object TheUniverse : UniversalObject("The Universe", ItemStack(Material.NETHER_STAR)) {
    override val orbiting: UniversalObject
        get() = error("The Universe is not orbiting anything")

    override val orbit = Orbit.lightYears(0.0, 0.0)
}