package io.github.addoncommunity.galactifun.api.objects

import io.github.addoncommunity.galactifun.units.Distance
import io.github.addoncommunity.galactifun.units.Distance.Companion.meters
import io.github.addoncommunity.galactifun.units.Mass
import io.github.addoncommunity.galactifun.units.SphericalPosition
import kotlinx.datetime.Instant
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class Star(
    name: String,
    override val mass: Mass,
    override val radius: Distance,
    val position: SphericalPosition
) : CelestialObject(name, ItemStack(Material.SUNFLOWER)) {

    override fun distanceTo(other: CelestialObject, time: Instant): Distance {
        return when (other) {
            this -> 0.0.meters
            is Star -> position.distanceTo(other.position)
            else -> other.distanceTo(this, time)
        }
    }
}