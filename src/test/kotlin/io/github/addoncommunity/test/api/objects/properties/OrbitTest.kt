package io.github.addoncommunity.test.api.objects.properties

import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.api.objects.properties.visVivaEquation
import io.github.addoncommunity.galactifun.base.BaseUniverse
import io.github.addoncommunity.galactifun.units.Angle
import io.github.addoncommunity.galactifun.units.Angle.Companion.degrees
import io.github.addoncommunity.galactifun.units.Angle.Companion.radians
import io.github.addoncommunity.galactifun.units.Distance.Companion.au
import io.github.addoncommunity.test.CommonTest
import io.kotest.matchers.doubles.percent
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Instant
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.time.DurationUnit

class OrbitTest : CommonTest() {

    lateinit var orbit: Orbit

    @BeforeEach
    fun setUpOrbit() {
        orbit = Orbit(
            parent = BaseUniverse.sun,
            semimajorAxis = 1.0.au,
            eccentricity = Orbit.TINY_ECCENTRICITY,
            longitudeOfPeriapsis = 0.0.degrees,
            timeOfPeriapsis = EPOCH
        )
        Orbit.TIME_SCALE = 1.0
    }

    @Test
    fun testPeriod() {
        orbit.period.toDouble(DurationUnit.DAYS) shouldBeRoughly 365.26
    }

    @Test
    fun testPosition() {
        val time = orbit.timeOfPeriapsis
        val pos = orbit.position(time)
        pos.radius shouldBe orbit.radius(time)
    }

    @Test
    fun testVelocity() {
        fun testVelocityAt(time: Instant, expectedAngle: Angle) {
            val vel = orbit.velocity(time)
            vel.length.meters shouldBeRoughly visVivaEquation(
                orbit.parent.gravitationalParameter,
                orbit.radius(orbit.timeOfPeriapsis),
                orbit.semimajorAxis
            )
            vel.polar.angle.standardForm.degrees shouldBeRoughly expectedAngle.degrees
        }

        testVelocityAt(orbit.timeOfPeriapsis, 90.0.degrees)
        testVelocityAt(orbit.timeOfPeriapsis + orbit.period, 90.0.degrees)
        testVelocityAt(orbit.timeOfPeriapsis + orbit.period / 2, 270.0.degrees)
        testVelocityAt(orbit.timeOfPeriapsis + orbit.period / 4, 180.0.degrees)
    }

    @Test
    fun testTimeOfFlight() {
        val time = orbit.timeOfPeriapsis
        val timeOfFlight = orbit.timeOfFlight(
            0.0.radians,
            orbit.meanAnomaly(time + orbit.period / 4)
        )
        timeOfFlight.inWholeDays shouldBe 91
    }
}

infix fun Double.shouldBeRoughly(expected: Double) = this shouldBe expected.plusOrMinus(0.1.percent)

val EPOCH = Instant.parse("1970-01-01T00:00:00Z")