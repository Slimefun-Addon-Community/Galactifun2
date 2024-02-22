package io.github.addoncommunity.galactifun.api.objects.properties

import io.github.addoncommunity.galactifun.api.objects.UniversalObject
import io.github.addoncommunity.galactifun.util.Constants
import io.github.addoncommunity.galactifun.util.LazyDouble
import io.github.addoncommunity.galactifun.util.units.Distance
import io.github.addoncommunity.galactifun.util.units.Distance.Companion.meters
import io.github.addoncommunity.galactifun.util.units.doubleSeconds
import kotlinx.datetime.Instant
import java.util.*
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Orbit(
    val parent: UniversalObject,
    val semimajorAxis: Distance,
    val eccentricity: Double,
    // Our orbits are always flat, so inclination is always 0, and we don't need to store it
    val argumentOfPeriapsis: Double,
    val timeOfPeriapsis: Instant
) {
    val meanMotion by LazyDouble {
        val a = semimajorAxis.meters
        sqrt(parent.gravitationalParameter / (a * a * a))
    }

    fun meanAnomaly(time: Instant): Double {
        val t = (time - timeOfPeriapsis) / Constants.ORBIT_TIME_SCALE
        return meanMotion * t.doubleSeconds
    }

    private val eccentricAnomalyCache = WeakHashMap<Instant, Double>()

    fun eccentricAnomaly(time: Instant): Double {
        return eccentricAnomalyCache.getOrPut(time) { kelpersEquation(meanAnomaly(time), eccentricity) }
    }

    fun trueAnomaly(time: Instant): Double {
        val cosE = cos(eccentricAnomaly(time))
        return acos((cosE - eccentricity) / (1 - eccentricity * cosE))
    }

    fun radius(time: Instant): Distance {
        val e = eccentricAnomaly(time)
        val a = semimajorAxis.meters
        return (a * (1 - eccentricity * cos(e))).meters
    }
}

tailrec fun kelpersEquation(m: Double, e: Double, guessE: Double = m): Double {
    val nextGuess = m + e * sin(guessE)
    return if (nextGuess == guessE) nextGuess else kelpersEquation(m, e, nextGuess)
}