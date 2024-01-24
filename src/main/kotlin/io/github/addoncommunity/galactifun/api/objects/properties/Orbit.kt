package io.github.addoncommunity.galactifun.api.objects.properties

import kotlin.math.PI
import kotlin.time.Duration
import kotlin.time.DurationUnit

class Orbit(val distance: Distance, year: Duration) {

    private val year = EARTH_YEAR * year.toDouble(DurationUnit.DAYS) / 365.25 * 1200000 // why 1200000? it was in the original code

    val position: Double
        get() {
            if (year == 0.0) return 0.0
            return (System.currentTimeMillis() % year) * PI * 2 / year
        }
}

/**
 * The number of Minecraft days in an Earth year
 */
private const val EARTH_YEAR = 30