package io.github.addoncommunity.galactifun.api.objects

import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.units.Angle.Companion.degrees
import io.github.addoncommunity.galactifun.units.Distance
import io.github.addoncommunity.galactifun.units.Mass
import io.github.addoncommunity.galactifun.util.Constants
import io.github.addoncommunity.galactifun.util.LazyDouble
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils
import kotlinx.datetime.Instant
import org.bukkit.inventory.ItemStack
import kotlin.math.sqrt

sealed class CelestialObject(name: String, baseItem: ItemStack) {

    val name = ChatUtils.removeColorCodes(name)
    val id = this.name.lowercase().replace(' ', '_')

    val item = CustomItemStack(baseItem, name)

    abstract fun distanceTo(other: CelestialObject, time: Instant): Distance



    abstract val mass: Mass
    abstract val radius: Distance

    val gravitationalParameter by LazyDouble { Constants.GRAVITATIONAL_CONSTANT * mass.kilograms }
    val escapeVelocity by LazyDouble { sqrt(2 * Constants.GRAVITATIONAL_CONSTANT * mass.kilograms / radius.kilometers) }
    val parkingOrbit: Orbit by lazy {
        Orbit(
            parent = this,
            semimajorAxis = radius * 1.1,
            eccentricity = 0.0,
            argumentOfPeriapsis = 0.0.degrees,
            timeOfPeriapsis = Instant.fromEpochMilliseconds(0)
        )
    }

    private val _orbiters = mutableListOf<CelestialObject>()
    val orbiters: List<CelestialObject> = _orbiters

    fun addOrbiter(orbiter: CelestialObject) {
        _orbiters.add(orbiter)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CelestialObject) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}