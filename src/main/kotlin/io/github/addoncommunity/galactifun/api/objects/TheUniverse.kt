package io.github.addoncommunity.galactifun.api.objects

import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.util.units.Distance
import io.github.addoncommunity.galactifun.util.units.Mass
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object TheUniverse : UniversalObject("The Universe", ItemStack(Material.NETHER_STAR)) {
    override val orbit: Orbit
        get() = error("The Universe does not have an orbit")
    override val mass: Mass
        get() = error("The Universe does not have a defined mass")
    override val radius: Distance
        get() = error("The Universe does not have a defined radius")
}