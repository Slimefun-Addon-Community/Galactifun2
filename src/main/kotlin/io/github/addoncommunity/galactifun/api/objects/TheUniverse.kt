package io.github.addoncommunity.galactifun.api.objects

import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.util.units.Distance
import io.github.addoncommunity.galactifun.util.units.Distance.Companion.lightYears
import io.github.addoncommunity.galactifun.util.units.Mass
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.time.Duration.Companion.days

object TheUniverse : UniversalObject("The Universe", ItemStack(Material.NETHER_STAR)) {
    override val orbiting: UniversalObject
        get() = error("The Universe is not orbiting anything")

    override val orbit = Orbit(0.lightYears, 0.days)
    override val mass: Mass
        get() = error("The Universe does nto have a defined mass")
    override val radius: Distance
        get() = error("The Universe does not have a defined radius")
}