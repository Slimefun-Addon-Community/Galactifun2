package io.github.addoncommunity.test.api.objects.properties

import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.api.objects.properties.visVivaEquation
import io.github.addoncommunity.galactifun.impl.BaseUniverse
import io.github.addoncommunity.galactifun.units.Angle
import io.github.addoncommunity.galactifun.units.Angle.Companion.degrees
import io.github.addoncommunity.galactifun.units.Angle.Companion.radians
import io.github.addoncommunity.galactifun.units.Distance.Companion.au
import io.github.addoncommunity.galactifun.units.standardForm
import io.github.addoncommunity.test.CommonTest
import kotlinx.datetime.Instant
import org.junit.jupiter.api.BeforeEach
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isIn
import kotlin.test.Test
import kotlin.time.DurationUnit

class OrbitTest : CommonTest() {

    lateinit var orbit: Orbit

    @BeforeEach
    fun setUpOrbit() {
        orbit = Orbit(
            parent = BaseUniverse.sun,
            semimajorAxis = 1.au,
            eccentricity = Orbit.TINY_ECCENTRICITY,
            longitudeOfPeriapsis = 0.degrees,
            timeOfPeriapsis = EPOCH
        )
        Orbit.TIME_SCALE = 1.0
    }

    @Test
    fun testPeriod() {
        expectThat(orbit.period.toDouble(DurationUnit.DAYS)).isRoughly(365.26)
    }

    @Test
    fun testPosition() {
        val time = orbit.timeOfPeriapsis
        val pos = orbit.position(time)
        expectThat(pos.radius).isEqualTo(orbit.radius(time))
    }

    @Test
    fun testVelocity() {
        fun testVelocityAt(time: Instant, expectedAngle: Angle) {
            val vel = orbit.velocity(time)
            expectThat(vel.length.meters).isRoughly(
                visVivaEquation(
                    orbit.parent.gravitationalParameter,
                    orbit.radius(orbit.timeOfPeriapsis),
                    orbit.semimajorAxis
                ).metersPerSecond
            )
            expectThat(vel.polar.angle.standardForm.degrees).isRoughly(expectedAngle.degrees)
        }

        testVelocityAt(orbit.timeOfPeriapsis, 90.degrees)
        testVelocityAt(orbit.timeOfPeriapsis + orbit.period, 90.degrees)
        testVelocityAt(orbit.timeOfPeriapsis + orbit.period / 2, 270.degrees)
        testVelocityAt(orbit.timeOfPeriapsis + orbit.period / 4, 180.degrees)
    }

    @Test
    fun testTimeOfFlight() {
        val time = orbit.timeOfPeriapsis
        val timeOfFlight = orbit.timeOfFlight(
            0.radians,
            orbit.meanAnomaly(time + orbit.period / 4)
        )
        expectThat(timeOfFlight.inWholeDays).isEqualTo(91)
    }
}

fun Assertion.Builder<Double>.isRoughly(expected: Double) = this
    .isIn((expected - expected * .1)..(expected + expected * .1))

val EPOCH = Instant.parse("1970-01-01T00:00:00Z")