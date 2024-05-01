package io.github.addoncommunity.galactifun.impl.objects.earth

import io.github.addoncommunity.galactifun.api.objects.planet.AlienWorld
import io.github.addoncommunity.galactifun.api.objects.planet.gen.WorldGenerator
import io.github.addoncommunity.galactifun.api.objects.properties.DayCycle
import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Atmosphere
import io.github.addoncommunity.galactifun.impl.BaseUniverse
import io.github.addoncommunity.galactifun.units.Angle.Companion.degrees
import io.github.addoncommunity.galactifun.units.Distance.Companion.kilometers
import io.github.addoncommunity.galactifun.units.Mass.Companion.kilograms
import kotlinx.datetime.Instant
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.metamechanists.displaymodellib.models.ModelBuilder
import org.metamechanists.displaymodellib.models.components.ModelCuboid
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

    override val moon: ModelBuilder = ModelBuilder()
        .add(
            "water", ModelCuboid()
                .material(Material.LAPIS_BLOCK)
                .scale(16f, 1f, 16f)
                .translate(8f, 0.5f, 8f)
        )
        .add(
            "na", ModelCuboid()
                .material(Material.GRASS_BLOCK)
                .scale(8f, 1.0f, 6f)
                .translate(11f, 0.6f, 3.1f)
        )
        .add(
            "sa", ModelCuboid()
                .material(Material.GRASS_BLOCK)
                .scale(8f, 1.0f, 2f)
                .translate(4f, 0.6f, 1.1f)
        )
        .add(
            "af", ModelCuboid()
                .material(Material.GRASS_BLOCK)
                .scale(7f, 1.0f, 7f)
                .translate(3.5f, 0.6f, 12.5f)
        )
        .add(
            "eu", ModelCuboid()
                .material(Material.GRASS_BLOCK)
                .scale(6f, 1.0f, 8f)
                .translate(11f, 0.6f, 12f)
        )
        .add(
            "north_pole", ModelCuboid()
                .material(Material.SNOW_BLOCK)
                .scale(2f, 1.0f, 6f)
                .translate(15f, 0.7f, 8f)
        )
        .add(
            "south_pole", ModelCuboid()
                .material(Material.SNOW_BLOCK)
                .scale(2f, 1.0f, 6f)
                .translate(1f, 0.7f, 8f)
        )
}