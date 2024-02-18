package io.github.addoncommunity.galactifun.api.objects

import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.util.Constants
import io.github.addoncommunity.galactifun.util.units.Distance
import io.github.addoncommunity.galactifun.util.units.Distance.Companion.lightYears
import io.github.addoncommunity.galactifun.util.units.Mass
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils
import org.bukkit.inventory.ItemStack
import kotlin.math.cos
import kotlin.math.sqrt

abstract class UniversalObject protected constructor(name: String, baseItem: ItemStack) {

    val name = ChatUtils.removeColorCodes(name)
    val id = this.name.lowercase().replace(' ', '_')

    val item = CustomItemStack(baseItem, name)

    abstract val orbiting: UniversalObject
    abstract val orbit: Orbit
    abstract val mass: Mass
    abstract val radius: Distance

    val escapeVelocity: Double
        get() = sqrt(2 * Constants.GRAVITATIONAL_CONSTANT * mass.kilograms / radius.kilometers)
    val parkingOrbit: Distance
        get() = radius / 10

    val orbitLevel: Int
        get() = if (this is TheUniverse) 0 else orbiting.orbitLevel + 1

    private val _orbiters = mutableListOf<UniversalObject>()
    val orbiters: List<UniversalObject> = _orbiters

    fun addOrbiter(orbiter: UniversalObject) {
        _orbiters.add(orbiter)
    }

    open fun distanceTo(other: UniversalObject): Distance {
        if (orbitLevel == 0 || orbitLevel < other.orbitLevel) {
            return other.orbit.distance + distanceTo(other.orbiting)
        }
        if (orbiting == other.orbiting) {
            val thisDist = orbit.distance.lightYears
            val otherDist = other.orbit.distance.lightYears
            val cosAngle = cos(orbit.position - other.orbit.position)
            return sqrt(thisDist * thisDist + otherDist * otherDist - 2 * thisDist * otherDist * cosAngle).lightYears
        }
        return orbit.distance + orbiting.distanceTo(other)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UniversalObject) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}