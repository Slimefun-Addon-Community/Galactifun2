package io.github.addoncommunity.galactifun.base.objects.earth

import io.github.addoncommunity.galactifun.api.objects.planet.AlienWorld
import io.github.addoncommunity.galactifun.api.objects.planet.gen.WorldGenerator
import io.github.addoncommunity.galactifun.api.objects.properties.DayCycle
import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Atmosphere
import io.github.addoncommunity.galactifun.base.BaseUniverse
import io.github.addoncommunity.galactifun.units.Angle.Companion.degrees
import io.github.addoncommunity.galactifun.units.Distance.Companion.kilometers
import io.github.addoncommunity.galactifun.units.Mass.Companion.kilograms
import kotlinx.datetime.Instant
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class Moon : AlienWorld("Moon", ItemStack(Material.END_STONE)) {
    override val atmosphere = Atmosphere.NONE
    override val dayCycle = DayCycle(29.days + 12.hours)
    override val orbit = Orbit(
        parent = BaseUniverse.earth,
        semimajorAxis = 384399.kilometers,
        eccentricity = 0.0549,
        longitudeOfPeriapsis = 0.degrees, // the argument changes over the course of the year
        timeOfPeriapsis = Instant.parse("2024-01-13T10:36:00Z")
    )
    override val mass = 7.346e22.kilograms
    override val radius = 1737.4.kilometers
    override val generator: WorldGenerator = MoonGenerator()
}