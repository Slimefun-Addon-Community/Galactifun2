package io.github.addoncommunity.galactifun.api.objects

import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.util.units.Distance
import io.github.addoncommunity.galactifun.util.units.Mass
import kotlinx.datetime.Instant
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class StarSystem(
    name: String,
    override val orbiting: UniversalObject,
    override val orbit: Orbit,
    override val mass: Mass,
    override val radius: Distance
) : UniversalObject(name, ItemStack(Material.SUNFLOWER)) {

    override fun distanceTo(other: UniversalObject, time: Instant): Distance {
        return if (orbitLevel > other.orbitLevel) {
            other.orbit.semimajorAxis + distanceTo(other.orbiting!!, time)
        } else if (orbitLevel == other.orbitLevel) {
            this.orbit.semimajorAxis - other.orbit.semimajorAxis
        } else {
            super.distanceTo(other, time)
        }
    }
}