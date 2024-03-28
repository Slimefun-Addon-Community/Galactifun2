package io.github.addoncommunity.galactifun.impl

import io.github.addoncommunity.galactifun.api.objects.MilkyWay
import io.github.addoncommunity.galactifun.api.objects.Star
import io.github.addoncommunity.galactifun.impl.objects.earth.Earth
import io.github.addoncommunity.galactifun.impl.objects.earth.Moon
import io.github.addoncommunity.galactifun.units.Angle.Companion.radians
import io.github.addoncommunity.galactifun.units.Distance.Companion.kilometers
import io.github.addoncommunity.galactifun.units.Distance.Companion.meters
import io.github.addoncommunity.galactifun.units.Mass.Companion.kilograms
import io.github.addoncommunity.galactifun.units.coordiantes.SphericalVector

object BaseUniverse {

    val sun = Star(
        name = "Sun",
        mass = 1.989e30.kilograms,
        radius = 695700.kilometers,
        // The sun is at the center of the universe, yay!
        position = SphericalVector(0.radians, 0.radians, 0.meters)
    )

    val earth = Earth()
    val moon = Moon()

    fun init() {
        MilkyWay.addStar(sun)
        earth.register()
        moon.register()
    }
}