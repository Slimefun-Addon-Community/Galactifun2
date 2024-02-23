package io.github.addoncommunity.galactifun.api.objects.properties

import io.github.addoncommunity.galactifun.api.objects.CelestialObject
import io.github.addoncommunity.galactifun.units.*
import io.github.addoncommunity.galactifun.units.Angle.Companion.radians
import io.github.addoncommunity.galactifun.units.Distance.Companion.meters
import io.github.addoncommunity.galactifun.units.coordiantes.CartesianVector
import io.github.addoncommunity.galactifun.units.coordiantes.PolarVector
import io.github.addoncommunity.galactifun.util.LazyDouble
import kotlinx.datetime.Instant
import java.util.*
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class Orbit(
    val parent: CelestialObject,
    val semimajorAxis: Distance, // a
    val eccentricity: Double, // e
    // Our orbits are always flat, so inclination is always 0
    // Longitude of the ascending node is also always 0
    val argumentOfPeriapsis: Angle, // ω
    val timeOfPeriapsis: Instant // T
) {

    init {
        require(eccentricity > 0) { "Eccentricity must be positive, use TINY_ECCENTRICITY for circular orbits" }
        require(eccentricity < 1) { "Eccentricity must be less than 1" }
    }

    val period: Duration by lazy {
        val a = semimajorAxis.meters
        (2 * Math.PI * sqrt(a * a * a / parent.gravitationalParameter)).seconds
    }

    val meanMotion by LazyDouble {
        val a = semimajorAxis.meters
        sqrt(parent.gravitationalParameter / (a * a * a))
    }

    fun meanAnomaly(time: Instant): Angle {
        val t = (time - timeOfPeriapsis) * TIME_SCALE
        return (meanMotion * t.doubleSeconds).radians
    }

    private val eccentricAnomalyCache = WeakHashMap<Instant, Angle>()

    fun eccentricAnomaly(time: Instant): Angle {
        return eccentricAnomalyCache.getOrPut(time) { kelpersEquation(meanAnomaly(time), eccentricity) }
    }

    private val beta by LazyDouble { eccentricity / (1 + sqrt(1 - eccentricity * eccentricity)) }

    fun trueAnomaly(time: Instant): Angle {
        val e = eccentricAnomaly(time)
        return atan2(beta * sin(e), 1 - beta * cos(e)).radians * 2.0 + e
    }

    fun radius(time: Instant): Distance {
        val e = eccentricAnomaly(time)
        val a = semimajorAxis.meters
        return (a * (1 - eccentricity * cos(e))).meters
    }

    fun position(time: Instant): PolarVector = PolarVector(radius(time), trueAnomaly(time))

    private val deltaTimeForVelocity: Duration by lazy { period / 1e6 }

    fun velocity(time: Instant): CartesianVector {
        // Do a tiny bit of calculus to find the velocity vector
        val deltaTime = time + deltaTimeForVelocity
        val pos1 = position(time)
        val pos2 = position(deltaTime)
        val w = eccentricAnomaly(time).degrees to eccentricAnomaly(deltaTime).degrees
        val z = trueAnomaly(time).degrees to trueAnomaly(deltaTime).degrees
        return (pos2.cartesian - pos1.cartesian) / deltaTimeForVelocity.doubleSeconds
    }

    companion object {
        /**
         * How much faster time is concerning orbital mechanics than in real life.
         * Minecraft is 72 times faster than real life, and a year is 12 times shorter than in real life.
         */
        var TIME_SCALE = 72.0 * 12.0

        /**
         * A tiny eccentricity to use for otherwise circular orbits in order to avoid maths problems.
         */
        const val TINY_ECCENTRICITY = 1e-6
    }
}

tailrec fun kelpersEquation(m: Angle, e: Double, guessE: Angle = m): Angle {
    val nextGuess = m + (e * sin(guessE)).radians
    return if (nextGuess == guessE) nextGuess else kelpersEquation(m, e, nextGuess)
}

fun visVivaEquation(mu: Double, r: Distance, a: Distance): Double =
    sqrt(mu * (2 / r.meters - 1 / a.meters))