package io.github.addoncommunity.galactifun.api.objects

import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.math.abs

class StarSystem(
    name: String,
    override val orbiting: UniversalObject,
    override val orbit: Orbit
) : UniversalObject(name, ItemStack(Material.SUNFLOWER)) {

    override fun distanceTo(other: UniversalObject): Double {
        return if (orbitLevel > other.orbitLevel) {
            other.orbit.distance + distanceTo(other.orbiting)
        } else if (orbitLevel == other.orbitLevel) {
            abs(this.orbit.distance - other.orbit.distance)
        } else {
            super.distanceTo(other)
        }
    }
}