package io.github.addoncommunity.galactifun.api.objects.planet

import io.github.addoncommunity.galactifun.api.objects.UniversalObject
import io.github.addoncommunity.galactifun.api.objects.properties.DayCycle
import io.github.addoncommunity.galactifun.api.objects.properties.OrbitPosition
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Atmosphere
import io.github.addoncommunity.galactifun.core.managers.PlanetManager
import io.github.seggan.kfun.location.plus
import org.bukkit.Location
import org.bukkit.inventory.ItemStack

abstract class PlanetaryObject(name: String, baseItem: ItemStack) : UniversalObject(name, baseItem) {

    abstract val dayCycle: DayCycle
    abstract val atmosphere: Atmosphere

    val orbitPosition: OrbitPosition
        get() = PlanetManager.getOrbit(this)

    fun getOrbitOffset(location: Location): Location {
        return orbitPosition.centerLocation + location
    }
}