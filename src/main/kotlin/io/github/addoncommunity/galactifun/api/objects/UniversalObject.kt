package io.github.addoncommunity.galactifun.api.objects

import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.util.Constants
import io.github.addoncommunity.galactifun.util.LazyDouble
import io.github.addoncommunity.galactifun.util.units.Distance
import io.github.addoncommunity.galactifun.util.units.Distance.Companion.meters
import io.github.addoncommunity.galactifun.util.units.Mass
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils
import kotlinx.datetime.Instant
import org.bukkit.inventory.ItemStack
import kotlin.math.cos
import kotlin.math.sqrt

abstract class UniversalObject protected constructor(name: String, baseItem: ItemStack) {

    val name = ChatUtils.removeColorCodes(name)
    val id = this.name.lowercase().replace(' ', '_')

    val item = CustomItemStack(baseItem, name)

    abstract val orbit: Orbit
    abstract val mass: Mass
    abstract val radius: Distance

    val gravitationalParameter by LazyDouble { Constants.GRAVITATIONAL_CONSTANT * mass.kilograms }
    val escapeVelocity by LazyDouble { sqrt(2 * Constants.GRAVITATIONAL_CONSTANT * mass.kilograms / radius.kilometers) }
    val parkingOrbit: Orbit by lazy {
        Orbit(
            parent = this,
            semimajorAxis = radius / 10,
            eccentricity = 0.0,
            argumentOfPeriapsis = 0.0,
            timeOfPeriapsis = Instant.fromEpochMilliseconds(0)
        )
    }

    val orbitLevel: Int
        get() = if (this is TheUniverse) 0 else orbit.parent.orbitLevel + 1

    private val _orbiters = mutableListOf<UniversalObject>()
    val orbiters: List<UniversalObject> = _orbiters

    fun addOrbiter(orbiter: UniversalObject) {
        _orbiters.add(orbiter)
    }

    open fun distanceTo(other: UniversalObject, time: Instant): Distance {
        if (orbitLevel == 0 || orbitLevel < other.orbitLevel) {
            return other.orbit.semimajorAxis + distanceTo(other.orbit.parent, time)
        }
        if (orbit.parent == other.orbit.parent) {
            val thisDist = orbit.radius(time).meters
            val otherDist = other.orbit.radius(time).meters
            val cosAngle = cos(orbit.trueAnomaly(time) - other.orbit.trueAnomaly(time))
            return sqrt(thisDist * thisDist + otherDist * otherDist - 2 * thisDist * otherDist * cosAngle).meters
        }
        return orbit.semimajorAxis + orbit.parent.distanceTo(other, time)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UniversalObject) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}