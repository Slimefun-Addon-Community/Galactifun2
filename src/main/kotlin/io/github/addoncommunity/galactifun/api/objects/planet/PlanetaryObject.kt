package io.github.addoncommunity.galactifun.api.objects.planet

import io.github.addoncommunity.galactifun.api.objects.UniversalObject
import io.github.addoncommunity.galactifun.api.objects.properties.DayCycle
import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.api.objects.properties.OrbitPosition
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Atmosphere
import io.github.addoncommunity.galactifun.core.managers.PlanetManager
import io.github.addoncommunity.galactifun.util.Constants
import io.github.addoncommunity.galactifun.util.units.Distance
import io.github.addoncommunity.galactifun.util.units.Distance.Companion.meters
import io.github.seggan.kfun.location.plus
import kotlinx.datetime.Instant
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

abstract class PlanetaryObject(name: String, baseItem: ItemStack) : UniversalObject(name, baseItem) {

    abstract val dayCycle: DayCycle
    abstract val atmosphere: Atmosphere

    val orbitPosition: OrbitPosition
        get() = PlanetManager.getOrbit(this)

    fun getOrbitOffset(location: Location): Location {
        return orbitPosition.centerLocation + location
    }

    fun getDeltaVForTransferTo(other: PlanetaryObject, time: Instant): Double {
        if (this == other) return 0.0
        val thisParents = generateSequence(this as UniversalObject) {
            if (it.orbitLevel == 0) null else it.orbit.parent
        }.toList()
        if (other in thisParents) {
            return other.getDeltaVForTransferTo(this, time)
        }
        val otherParents = generateSequence(other as UniversalObject) {
            if (it.orbitLevel == 0) null else it.orbit.parent
        }.toList()
        if (this in otherParents) {
            var height = other.parkingOrbit
            var dV = 0.0
            for (obj in otherParents) {
                if (obj == this) break
                dV += abs(obj.escapeVelocity - visViva(obj.gravitationalParameter, height.radius(time), height.semimajorAxis))
                height = obj.orbit
            }
            dV += hohmannTransfer(height, parkingOrbit, time)
            return dV
        } else {
            val closestParent = thisParents.first { it in otherParents }
            val thisClosestSibling = thisParents[thisParents.indexOf(closestParent) - 1] as PlanetaryObject
            val thisClosestOrbit = thisClosestSibling.orbit
            val otherClosestSibling = otherParents[otherParents.indexOf(closestParent) - 1] as PlanetaryObject
            val otherClosestOrbit = otherClosestSibling.orbit

            TODO()
        }
    }
}

// These formulas came from http://www.braeunig.us/space/

private fun visViva(mu: Double, r: Distance, a: Distance): Double =
    sqrt(mu * (2 / r.meters - 1 / a.meters))

private fun hohmannTransfer(parking: Orbit, target: Orbit, time: Instant): Double {
    val parkingR = parking.radius(time)
    val targetR = target.radius(time)
    val transferA = (parkingR + targetR) / 2
    val mu = parking.parent.gravitationalParameter
    val firstManeuver = abs(visViva(mu, parkingR, transferA) - visViva(mu, parkingR, parking.semimajorAxis))
    val secondManeuver = abs(visViva(mu, targetR, target.semimajorAxis) - visViva(mu, targetR, transferA))
    return firstManeuver + secondManeuver
}

// https://math.stackexchange.com/a/407425/1291722
// https://www.desmos.com/calculator/mdifr3y167
private fun oneTangentTransferOrbit(
    parkingR: Distance,
    parkingTheta: Double,
    targetR: Distance,
    targetTheta: Double
): Distance? {
    // Rotate the coordinate system so that the parking orbit is on the x-axis
    val v = parkingR.meters
    val p = targetR.meters * cos(targetTheta - parkingTheta)
    val q = targetR.meters * sin(targetTheta - parkingTheta)

    // Calculate the transfer orbit's semimajor axis
    val a = v * (v - p) / (2 * v - p - sqrt(p * p + q * q))
    if (a <= 0) {
        // The transfer orbit is either parabolic or hyperbolic
        return null
    }
    return a.meters
}

private fun brachistochroneTransfer(
    distance: Distance,

): BrachistochroneTransfer {
    val time = 2 * sqrt(distance.meters / Constants.EARTH_GRAVITY)
    val dV = Constants.EARTH_GRAVITY * time
    return BrachistochroneTransfer(dV, time.seconds)
}

private data class BrachistochroneTransfer(
    val deltaV: Double,
    val time: Duration
)
