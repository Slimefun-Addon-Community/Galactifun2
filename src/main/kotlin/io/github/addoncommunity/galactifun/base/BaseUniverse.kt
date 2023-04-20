package io.github.addoncommunity.galactifun.base

import io.github.addoncommunity.galactifun.api.objects.Galaxy
import io.github.addoncommunity.galactifun.api.objects.StarSystem
import io.github.addoncommunity.galactifun.api.objects.TheUniverse
import io.github.addoncommunity.galactifun.api.objects.planet.PlanetaryWorld
import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.base.objects.earth.Earth
import io.github.addoncommunity.galactifun.base.objects.earth.Moon
import org.bukkit.Material

object BaseUniverse {

    val milkyWay = Galaxy(
        "Milky Way",
        Material.MILK_BUCKET,
        TheUniverse,
        Orbit.lightYears(12000000000.0, 0.0)
    )

    val solarSystem = StarSystem(
        "Solar System",
        milkyWay,
        Orbit.lightYears(27000.0, 250000000.0)
    )

    val earth = Earth()
    val moon = Moon()

    fun init() {
        listOf(earth, moon)
            .forEach(PlanetaryWorld::register)
    }
}