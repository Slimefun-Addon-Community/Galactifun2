package io.github.addoncommunity.galactifun.api.objects.planet

import io.github.addoncommunity.galactifun.api.objects.UniversalObject
import io.github.addoncommunity.galactifun.api.objects.properties.DayCycle
import org.bukkit.inventory.ItemStack

abstract class PlanetaryObject(name: String, baseItem: ItemStack) : UniversalObject(name, baseItem) {
    abstract val dayCycle: DayCycle
}