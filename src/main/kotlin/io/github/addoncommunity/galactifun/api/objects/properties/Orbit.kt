package io.github.addoncommunity.galactifun.api.objects.properties

import io.github.addoncommunity.galactifun.api.objects.CelestialObject
import io.github.addoncommunity.galactifun.units.*
import io.github.addoncommunity.galactifun.units.Angle.Companion.radians
import io.github.addoncommunity.galactifun.units.Distance.Companion.meters
import io.github.addoncommunity.galactifun.util.LazyDouble
import kotlinx.datetime.Instant
import org.joml.Vector2d
import java.util.*
import kotlin.math.acos
import kotlin.math.sqrt

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

    fun trueAnomaly(time: Instant): Angle {
        val cosE = cos(eccentricAnomaly(time))
        return acos((cosE - eccentricity) / (1 - eccentricity * cosE)).radians
    }

    fun radius(time: Instant): Distance {
        val e = eccentricAnomaly(time)
        val a = semimajorAxis.meters
        return (a * (1 - eccentricity * cos(e))).meters
    }

    val sinOmega by LazyDouble { sin(argumentOfPeriapsis) }
    val cosOmega by LazyDouble { cos(argumentOfPeriapsis) }

    fun velocityVectorAt(time: Instant): Vector2d {
        val p = semimajorAxis.meters * (1 - eccentricity * eccentricity)
        val h = sqrt(parent.gravitationalParameter * p)
        val r = radius(time).meters
        val nu = trueAnomaly(time)
        val sinOmegaNu = sin(argumentOfPeriapsis + nu)
        val cosOmegaNu = cos(argumentOfPeriapsis + nu)

        val x = r * (cosOmega * cosOmegaNu - sinOmega * sinOmegaNu)
        val y = r * (sinOmega * cosOmegaNu + cosOmega * sinOmegaNu)

        val magic = (h * eccentricity) / (r * p) * sin(nu)
        val vx = x * magic - h / r * (cosOmega * sinOmegaNu + sinOmega * cosOmegaNu)
        val vy = y * magic - h / r * (sinOmega * sinOmegaNu - cosOmega * cosOmegaNu)
        return Vector2d(vx, vy)
    }

    companion object {
        /**
         * How much faster time is concerning orbital mechanics than in real life.
         * Minecraft is 72 times faster than real life, and a year is 12 times shorter than in real life.
         */
        const val TIME_SCALE = 72.0 * 12.0

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