package io.github.addoncommunity.galactifun.base

import io.github.addoncommunity.galactifun.api.objects.Galaxy
import io.github.addoncommunity.galactifun.api.objects.StarSystem
import io.github.addoncommunity.galactifun.api.objects.TheUniverse
import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.base.objects.earth.Earth
import io.github.addoncommunity.galactifun.base.objects.earth.Moon
import io.github.addoncommunity.galactifun.util.units.Distance.Companion.kilometers
import io.github.addoncommunity.galactifun.util.units.Distance.Companion.lightYears
import io.github.addoncommunity.galactifun.util.units.Mass.Companion.kilograms
import io.github.addoncommunity.galactifun.util.units.years
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object BaseUniverse {

    val milkyWay = Galaxy(
        "Milky Way",
        ItemStack(Material.MILK_BUCKET),
        TheUniverse,
        Orbit(12000000000.lightYears, 0.years),
        3e42.kilograms,
        100000.lightYears
    )

    val solarSystem = StarSystem(
        "Solar System",
        milkyWay,
        Orbit(27000.lightYears, 250000000.years),
        1.988e30.kilograms,
        695700.kilometers
    )

    val earth = Earth()
    val moon = Moon()

    fun init() {
        earth.register()
        moon.register()
    }
}