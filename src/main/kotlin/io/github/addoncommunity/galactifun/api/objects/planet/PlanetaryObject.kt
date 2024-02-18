package io.github.addoncommunity.galactifun.api.objects.planet

import io.github.addoncommunity.galactifun.api.objects.UniversalObject
import io.github.addoncommunity.galactifun.api.objects.properties.DayCycle
import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.api.objects.properties.OrbitPosition
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Atmosphere
import io.github.addoncommunity.galactifun.core.managers.PlanetManager
import io.github.addoncommunity.galactifun.util.Constants
import io.github.addoncommunity.galactifun.util.units.Distance
import io.github.seggan.kfun.location.plus
import org.apache.commons.lang3.ObjectUtils.max
import org.apache.commons.lang3.ObjectUtils.min
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import kotlin.math.*

abstract class PlanetaryObject(name: String, baseItem: ItemStack) : UniversalObject(name, baseItem) {

    abstract val dayCycle: DayCycle
    abstract val atmosphere: Atmosphere

    val orbitPosition: OrbitPosition
        get() = PlanetManager.getOrbit(this)

    fun getOrbitOffset(location: Location): Location {
        return orbitPosition.centerLocation + location
    }

    fun getDeltaVForTransferTo(other: PlanetaryObject): Double {
        if (this == other) return 0.0
        val thisParents = generateSequence(this as UniversalObject) { it.orbiting }.toList()
        if (other in thisParents) {
            return other.getDeltaVForTransferTo(this)
        }
        val otherParents = generateSequence(other as UniversalObject) { it.orbiting }.toList()
        if (this in otherParents) {
            var height = other.parkingOrbit
            var dV = 0.0
            for (obj in otherParents) {
                if (obj == this) break
                dV += abs(obj.escapeVelocity - visViva(obj.mu, height, height))
                height = obj.orbit.semimajorAxis
            }
            dV += hohmannTransfer(mass.kilograms * Constants.GRAVITATIONAL_CONSTANT, height, parkingOrbit)
            return dV
        } else {
            val closestParent = thisParents.first { it in otherParents }
            val thisClosestSibling = thisParents[thisParents.indexOf(closestParent) - 1] as PlanetaryObject
            val thisClosestOrbit = thisClosestSibling.orbit
            val otherClosestSibling = otherParents[otherParents.indexOf(closestParent) - 1] as PlanetaryObject
            val otherClosestOrbit = otherClosestSibling.orbit

            // Perform a binary search to find the most efficient transfer we can get
            // I *could* use Newton's method for better efficiency, but my brain is
            // wrecked by all the math I've done today and I don't want to think about it
            var min = min(thisClosestOrbit.semimajorAxis, otherClosestOrbit.semimajorAxis)
            var max = max(thisClosestOrbit.semimajorAxis, otherClosestOrbit.semimajorAxis) * 4
            var iterations = 0
            var minDiff: OrbitalParameters
            do {
                val testAxis = (max + min) / 2
                minDiff = anomalyDifference(testAxis, thisClosestOrbit, otherClosestOrbit)
                if (minDiff.anomalyDiff > 0) {
                    min = testAxis
                } else {
                    max = testAxis
                }
            } while (abs(minDiff.anomalyDiff) > PI / 16 && iterations++ < 64)

            val phi = atan(
                minDiff.eccentricity * sin(minDiff.trueAnomaly) /
                        (1 + minDiff.eccentricity * cos(minDiff.trueAnomaly))
            )
            val vfb = visViva(otherClosestSibling.mu, otherClosestOrbit.semimajorAxis, otherClosestOrbit.semimajorAxis)
            val vtxb = visViva(otherClosestSibling.mu, otherClosestOrbit.semimajorAxis, minDiff.semimajorAxis)
            val deltaVb = sqrt(vtxb * vtxb + vfb * vfb - 2 * vtxb * vfb * cos(phi))
            val via = visViva(thisClosestSibling.mu, thisClosestOrbit.semimajorAxis, thisClosestOrbit.semimajorAxis)
            val vtxa = visViva(thisClosestSibling.mu, thisClosestOrbit.semimajorAxis, minDiff.semimajorAxis)
            val deltaVa = vtxa - via
            return deltaVa + deltaVb + getDeltaVForTransferTo(thisClosestSibling) + other.getDeltaVForTransferTo(otherClosestSibling)
        }
    }
}

// These formulas came from http://www.braeunig.us/space/

private fun visViva(mu: Double, r: Distance, a: Distance): Double =
    sqrt(mu * (2 / r.kilometers - 1 / a.kilometers))

private fun hohmannTransfer(mu: Double, parkingR: Distance, targetR: Distance): Double {
    val transferA = (parkingR + targetR) / 2
    val firstManeuver = abs(visViva(mu, parkingR, transferA) - visViva(mu, parkingR, parkingR))
    val secondManeuver = abs(visViva(mu, targetR, targetR) - visViva(mu, targetR, transferA))
    return firstManeuver + secondManeuver
}

private fun anomalyDifference(transferA: Distance, source: Orbit, target: Orbit): OrbitalParameters {
    val e = 1 - source.semimajorAxis.kilometers / transferA.kilometers
    val trueAnomaly = acos(
        (transferA.kilometers * (1 - e * e) / target.semimajorAxis.kilometers - 1) / e
    )
    return OrbitalParameters(transferA, e, trueAnomaly, trueAnomaly - target.trueAnomaly)
}

private data class OrbitalParameters(
    val semimajorAxis: Distance,
    val eccentricity: Double,
    val trueAnomaly: Double,
    val anomalyDiff: Double
)