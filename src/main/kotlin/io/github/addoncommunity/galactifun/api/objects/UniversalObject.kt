package io.github.addoncommunity.galactifun.api.objects

import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.bakedlibs.dough.items.CustomItemStack
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

    @Suppress("LeakingThis")
    val orbitLevel: Int
        get() = if (this is TheUniverse) 0 else orbiting.orbitLevel + 1

    private val _orbiters = mutableListOf<UniversalObject>()
    val orbiters: List<UniversalObject>
        get() = _orbiters.toList()

    fun addOrbiter(orbiter: UniversalObject) {
        _orbiters.add(orbiter)
    }

    open fun distanceTo(other: UniversalObject): Double {
        if (orbitLevel == 0 || orbitLevel < other.orbitLevel) {
            return other.orbit.distance + distanceTo(other.orbiting)
        }
        if (orbiting == other.orbiting) {
            val thisDist = orbit.distance
            val otherDist = other.orbit.distance
            val cosAngle = cos(orbit.position - other.orbit.position)
            return sqrt(thisDist * thisDist + otherDist * otherDist - 2 * thisDist * otherDist * cosAngle)
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