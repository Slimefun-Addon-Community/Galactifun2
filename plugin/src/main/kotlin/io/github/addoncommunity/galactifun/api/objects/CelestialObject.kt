package io.github.addoncommunity.galactifun.api.objects

import io.github.addoncommunity.galactifun.GRAVITATIONAL_CONSTANT
import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.units.Acceleration.Companion.metersPerSecondSquared
import io.github.addoncommunity.galactifun.units.Angle.Companion.degrees
import io.github.addoncommunity.galactifun.units.Distance
import io.github.addoncommunity.galactifun.units.Mass
import io.github.addoncommunity.galactifun.units.Velocity.Companion.metersPerSecond
import io.github.addoncommunity.galactifun.util.bukkit.plus
import io.github.addoncommunity.galactifun.util.general.LazyDouble
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils
import kotlinx.datetime.Instant
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.inventory.ItemStack
import kotlin.math.sqrt

sealed class CelestialObject(name: String, baseItem: ItemStack) {

    val name = ChatUtils.removeColorCodes(name)
    val id = this.name.lowercase().replace(' ', '_')

    val item: ItemStack by lazy {
        CustomItemStack(baseItem) { meta ->
            meta.displayName(Component.text(name))
            val lore = mutableListOf<String>()
            addLoreProperties(lore)
            meta.lore(lore.map { NamedTextColor.GRAY + it })
        }
    }

    abstract val mass: Mass
    abstract val radius: Distance

    val gravity by lazy {
        (GRAVITATIONAL_CONSTANT * mass.kilograms / (radius.meters * radius.meters)).metersPerSecondSquared
    }

    val gravitationalParameter by LazyDouble { GRAVITATIONAL_CONSTANT * mass.kilograms }
    val escapeVelocity by lazy { sqrt(2 * GRAVITATIONAL_CONSTANT * mass.kilograms / radius.meters).metersPerSecond }
    val parkingOrbit: Orbit by lazy {
        Orbit(
            parent = this,
            semimajorAxis = radius * 1.1,
            eccentricity = Orbit.TINY_ECCENTRICITY,
            longitudeOfPeriapsis = 0.degrees,
            timeOfPeriapsis = Instant.fromEpochMilliseconds(0)
        )
    }

    private val _orbiters = mutableListOf<CelestialObject>()
    val orbiters: List<CelestialObject> = _orbiters

    fun addOrbiter(orbiter: CelestialObject) {
        _orbiters.add(orbiter)
    }

    abstract fun distanceTo(other: CelestialObject, time: Instant): Distance

    protected open fun addLoreProperties(lore: MutableList<String>) {
        lore.add("Mass: $mass")
        lore.add("Radius: $radius")
        lore.add("Gravity: $gravity")
        lore.add("Escape Velocity: $escapeVelocity")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CelestialObject) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}