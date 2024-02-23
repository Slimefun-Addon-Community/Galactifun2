package io.github.addoncommunity.galactifun.base

import io.github.addoncommunity.galactifun.api.objects.Star
import io.github.addoncommunity.galactifun.base.objects.earth.Earth
import io.github.addoncommunity.galactifun.base.objects.earth.Moon
import io.github.addoncommunity.galactifun.units.Angle.Companion.radians
import io.github.addoncommunity.galactifun.units.Distance.Companion.kilometers
import io.github.addoncommunity.galactifun.units.Distance.Companion.meters
import io.github.addoncommunity.galactifun.units.Mass.Companion.kilograms
import io.github.addoncommunity.galactifun.units.SphericalPosition

object BaseUniverse {

    val sun = Star(
        name = "Sun",
        mass = 1.989e30.kilograms,
        radius = 695700.0.kilometers,
        // The sun is at the center of the universe, yay!
        position = SphericalPosition(0.0.radians, 0.0.radians, 0.0.meters)
    )

    val earth = Earth()
    val moon = Moon()

    fun init() {
        earth.register()
        moon.register()
    }
}