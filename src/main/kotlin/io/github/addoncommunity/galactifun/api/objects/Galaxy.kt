package io.github.addoncommunity.galactifun.api.objects

import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.util.units.Distance
import io.github.addoncommunity.galactifun.util.units.Mass
import org.bukkit.inventory.ItemStack

class Galaxy(
    name: String,
    baseItem: ItemStack,
    override val orbiting: UniversalObject,
    override val orbit: Orbit,
    override val mass: Mass,
    override val radius: Distance
) : UniversalObject(name, baseItem)