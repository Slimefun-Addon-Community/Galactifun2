package io.github.addoncommunity.galactifun.base

import io.github.addoncommunity.galactifun.api.objects.Galaxy
import io.github.addoncommunity.galactifun.api.objects.StarSystem
import io.github.addoncommunity.galactifun.api.objects.TheUniverse
import io.github.addoncommunity.galactifun.api.objects.properties.Distance.Companion.lightYears
import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.base.objects.earth.Earth
import io.github.addoncommunity.galactifun.base.objects.earth.Moon
import io.github.addoncommunity.galactifun.util.years
import org.bukkit.Material

object BaseUniverse {

    val milkyWay = Galaxy(
        "Milky Way",
        Material.MILK_BUCKET,
        TheUniverse,
        Orbit(12000000000.lightYears, 0.years)
    )

    val solarSystem = StarSystem(
        "Solar System",
        milkyWay,
        Orbit(27000.lightYears, 250000000.years)
    )

    val earth = Earth()
    val moon = Moon()

    fun init() {
        earth.register()
        moon.register()
    }
}