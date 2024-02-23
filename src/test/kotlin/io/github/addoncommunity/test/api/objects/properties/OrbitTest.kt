package io.github.addoncommunity.test.api.objects.properties

import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.base.BaseUniverse
import io.github.addoncommunity.galactifun.units.Angle.Companion.degrees
import io.github.addoncommunity.galactifun.units.Distance.Companion.kilometers
import io.github.addoncommunity.test.CommonTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

class OrbitTest : CommonTest() {

    @Test
    fun testOrbit() {
        val orbit = Orbit(
            parent = BaseUniverse.earth,
            semimajorAxis = 1000.0.kilometers,
            eccentricity = 1e-8,
            argumentOfPeriapsis = 0.0.degrees,
            timeOfPeriapsis = Instant.fromEpochMilliseconds(0)
        )
        val time = Instant.fromEpochMilliseconds(0)
        println(orbit.trueAnomaly(time))
        println(orbit.trueAnomaly(time + 20.minutes))
    }
}