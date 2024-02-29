package io.github.addoncommunity.galactifun.api.objects.properties

import io.github.addoncommunity.galactifun.Constants
import io.github.addoncommunity.galactifun.api.objects.CelestialObject
import io.github.addoncommunity.galactifun.pluginInstance
import io.github.addoncommunity.galactifun.units.*
import io.github.addoncommunity.galactifun.units.Angle.Companion.radians
import io.github.addoncommunity.galactifun.units.Distance.Companion.meters
import io.github.addoncommunity.galactifun.units.Velocity.Companion.metersPerSecond
import io.github.addoncommunity.galactifun.units.coordiantes.CartesianVector
import io.github.addoncommunity.galactifun.units.coordiantes.PolarVector
import io.github.addoncommunity.galactifun.util.Either
import io.github.addoncommunity.galactifun.util.LazyDouble
import kotlinx.datetime.Instant
import java.util.*
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

data class Orbit(
    val parent: CelestialObject,
    val semimajorAxis: Distance, // a
    val eccentricity: Double, // e
    // Our orbits are always flat, so inclination is always 0
    // Longitude of the ascending node is also always 0
    val longitudeOfPeriapsis: Angle, // ω + Ω
    val timeOfPeriapsis: Instant // T
) {

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

    private val dt: Duration by lazy { period / 1e6 }

    fun velocity(time: Instant): CartesianVector {
        // Do a tiny bit of calculus to find the velocity vector
        val deltaTime = time + dt
        val pos1 = position(time)
        val pos2 = position(deltaTime)
        return (pos2.cartesian - pos1.cartesian) / dt.doubleSeconds
    }

    fun timeOfFlight(meanAnomalyA: Angle, meanAnomalyB: Angle): Duration {
        return ((meanAnomalyB - meanAnomalyA).radians / meanMotion).seconds
    }

    fun hohmannTransfer(target: Orbit, time: Instant): Velocity {
        val parkingR = radius(time)
        val targetR = target.radius(time)
        val transferA = (parkingR + targetR) / 2.0
        val mu = parent.gravitationalParameter
        val firstManeuver = abs(
            visVivaEquation(mu, parkingR, transferA)
                    - visVivaEquation(mu, parkingR, semimajorAxis)
        )
        val secondManeuver = abs(
            visVivaEquation(mu, targetR, target.semimajorAxis) -
                    visVivaEquation(mu, targetR, transferA)
        )
        return firstManeuver + secondManeuver
    }

    fun arbitraryTransfer(targetOrbit: Orbit, time: Instant): Velocity {
        require(parent == targetOrbit.parent) { "Both orbits must have the same parent" }

        val parking = position(time)
        var targetTime = time
        var target = targetOrbit.position(targetTime)
        val transfers = mutableMapOf<Angle, Either<Orbit, BrachistochroneTransfer>>()

        var iterations = 0
        do {
            val intersectLongitude = target.angle + targetOrbit.longitudeOfPeriapsis
            val transfer1 = computeTransfer(parking, target, targetTime, intersectLongitude)
            val transfer2 = computeTransfer(parking, target, targetTime + 1.days, intersectLongitude)
            val diff1 = getAnomalyDifference(transfer1, time, targetOrbit, intersectLongitude)
            val diff2 = getAnomalyDifference(transfer2, time, targetOrbit, intersectLongitude)
            val minDiff = minOf(diff1, diff2).standardForm
            val timeDelta = 1.days * minDiff.radians
            if (diff1 < diff2) {
                transfers[diff1] = transfer1.method
                targetTime -= timeDelta
                target = targetOrbit.position(targetTime)
            } else {
                transfers[diff2] = transfer2.method
                targetTime += timeDelta
                target = targetOrbit.position(targetTime)
            }
        } while (iterations++ != MAX_ITERATIONS && minDiff.radians > 1e-3)

        if (iterations == MAX_ITERATIONS + 1) {
            pluginInstance.logger.warning("Failed to find a transfer orbit after $iterations iterations")
        }

        return when (val transfer = transfers.minBy { it.key }.value) {
            is Either.Left -> {
                val transferOrbit = transfer.value
                val transferVel = transferOrbit.velocity(targetTime)
                val targetVel = targetOrbit.velocity(targetTime)
                val burn2 = transferVel.distanceTo(targetVel).meters.metersPerSecond

                val burn1 = abs(
                    visVivaEquation(parent.gravitationalParameter, parking.radius, transferOrbit.semimajorAxis) -
                            visVivaEquation(parent.gravitationalParameter, parking.radius, semimajorAxis)
                )

                burn1 + burn2
            }

            is Either.Right -> transfer.value.deltaV
        }
    }

    private fun computeTransfer(
        parking: PolarVector,
        target: PolarVector,
        time: Instant,
        intersectLongitude: Angle
    ): Transfer {
        val transferA = oneTangentTransferOrbit(parking, target)
        if (transferA != null) {
            val transferOrbit = Orbit(
                parent = parent,
                semimajorAxis = transferA,
                eccentricity = 1 - parking.radius.meters / transferA.meters,
                longitudeOfPeriapsis = parking.angle + longitudeOfPeriapsis,
                timeOfPeriapsis = time
            )
            val intersectTrueAnomaly = intersectLongitude - transferOrbit.longitudeOfPeriapsis
            val mean = meanAnomalyFromTrueAnomaly(transferOrbit.eccentricity, intersectTrueAnomaly)
            return Transfer(Either.Left(transferOrbit), transferOrbit.timeOfFlight(0.radians, mean))
        } else {
            val distance = parking.distanceTo(target)
            val transferBrachistochrone = brachistochroneTransfer(distance)
            return Transfer(Either.Right(transferBrachistochrone), transferBrachistochrone.time)
        }
    }
}

private const val MAX_ITERATIONS = 512

private fun getAnomalyDifference(
    transfer: Transfer,
    time: Instant,
    targetOrbit: Orbit,
    intersectLongitude: Angle
): Angle {
    val targetTime = time + transfer.tof
    val target = targetOrbit.position(targetTime)
    return abs(target.angle + targetOrbit.longitudeOfPeriapsis - intersectLongitude).standardForm
}

private tailrec fun kelpersEquation(m: Angle, e: Double, guessE: Angle = m): Angle {
    val nextGuess = m + (e * sin(guessE)).radians
    return if (abs(nextGuess - guessE).radians < 1e-6) nextGuess else kelpersEquation(m, e, nextGuess)
}

fun visVivaEquation(mu: Double, r: Distance, a: Distance): Velocity =
    sqrt(mu * (2 / r.meters - 1 / a.meters)).metersPerSecond

// https://math.stackexchange.com/a/407425/1291722
// https://www.desmos.com/calculator/mdifr3y167
private fun oneTangentTransferOrbit(
    parking: PolarVector,
    target: PolarVector
): Distance? {
    // Rotate the coordinate system so that the parking orbit is on the x-axis
    val v = parking.radius.meters
    val p = target.radius.meters * cos(target.angle - parking.angle)
    val q = target.radius.meters * sin(target.angle - parking.angle)

    // Calculate the transfer orbit's semimajor axis
    var a = v * (v - p) / (2 * v - p - sqrt(p * p + q * q))
    if (a <= 0) return null
    if (a < v) {
        a = v * (v - p) / (2 * v - p + sqrt(p * p + q * q))
    }
    if (a <= 0) return null
    return a.meters
}

private fun brachistochroneTransfer(
    distance: Distance,
    acceleration: Acceleration = Constants.EARTH_GRAVITY / 16
): BrachistochroneTransfer {
    val time = 2.seconds * sqrt(distance.meters / acceleration.metersPerSecondSquared)
    return BrachistochroneTransfer(acceleration * time, time)
}

private data class BrachistochroneTransfer(
    val deltaV: Velocity,
    val time: Duration
)

private fun meanAnomalyFromTrueAnomaly(eccentricity: Double, trueAnomaly: Angle): Angle {
    val sqrt1me2sinF = sqrt(1 - eccentricity * eccentricity) * sin(trueAnomaly)
    val x1 = atan2(-sqrt1me2sinF, -eccentricity - cos(trueAnomaly))
    return (x1 + PI - eccentricity * sqrt1me2sinF / (1 + eccentricity * cos(trueAnomaly))).radians
}

private data class Transfer(
    val method: Either<Orbit, BrachistochroneTransfer>,
    val tof: Duration
)