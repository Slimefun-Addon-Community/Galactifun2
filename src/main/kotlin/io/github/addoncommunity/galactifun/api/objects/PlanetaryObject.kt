package io.github.addoncommunity.galactifun.api.objects

import io.github.addoncommunity.galactifun.Constants
import io.github.addoncommunity.galactifun.api.objects.properties.DayCycle
import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.api.objects.properties.OrbitPosition
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Atmosphere
import io.github.addoncommunity.galactifun.api.objects.properties.visVivaEquation
import io.github.addoncommunity.galactifun.core.managers.PlanetManager
import io.github.addoncommunity.galactifun.units.Distance
import io.github.addoncommunity.galactifun.units.Distance.Companion.meters
import io.github.addoncommunity.galactifun.units.cos
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

abstract class PlanetaryObject(name: String, baseItem: ItemStack) : CelestialObject(name, baseItem) {

    abstract val dayCycle: DayCycle
    abstract val atmosphere: Atmosphere
    abstract val orbit: Orbit

    val star: Star by lazy {
        val parent = orbit.parent
        if (parent is Star) parent else (parent as PlanetaryObject).star
    }

    val orbitPosition: OrbitPosition
        get() = PlanetManager.getOrbit(this)

    fun getOrbitOffset(location: Location): Location {
        return orbitPosition.centerLocation + location
    }

    override fun distanceTo(other: CelestialObject, time: Instant): Distance {
        if (other == this) return 0.0.meters
        if (other is Star) {
            if (star == other) {
                var dist = orbit.radius(time)
                if (orbit.parent != other) {
                    dist += orbit.parent.distanceTo(other, time)
                }
                return dist
            } else {
                return star.distanceTo(other, time) + distanceTo(star, time)
            }
        }

        require(other is PlanetaryObject)
        if (star == other.star) {
            if (orbit.parent == other.orbit.parent) {
                val thisDist = orbit.radius(time).meters
                val otherDist = other.orbit.radius(time).meters
                val cosAngle = cos(orbit.trueAnomaly(time) - other.orbit.trueAnomaly(time))
                return sqrt(thisDist * thisDist + otherDist * otherDist - 2 * thisDist * otherDist * cosAngle).meters
            }
            return orbit.radius(time) + orbit.parent.distanceTo(other, time)
        } else {
            return other.distanceTo(star, time) + distanceTo(star, time)
        }
    }

    fun getDeltaVForTransferTo(other: PlanetaryObject, time: Instant): Double {
        if (this == other) return 0.0
        val thisParents = generateSequence(this as CelestialObject) {
            if (it is PlanetaryObject) it.orbit.parent else null
        }.toList()
        if (other in thisParents) {
            return other.getDeltaVForTransferTo(this, time)
        }
        val otherParents = generateSequence(other as CelestialObject) {
            if (it is PlanetaryObject) it.orbit.parent else null
        }.toList()
        if (this in otherParents) {
            var height = other.parkingOrbit
            var dV = 0.0
            for (obj in otherParents) {
                if (obj == this) break
                dV += abs(
                    obj.escapeVelocity - visVivaEquation(
                        obj.gravitationalParameter,
                        height.radius(time),
                        height.semimajorAxis
                    )
                )
                height = (obj as PlanetaryObject).orbit
            }
            dV += hohmannTransfer(height, parkingOrbit, time)
            return dV
        } else {
            TODO()
        }
    }
}

private fun hohmannTransfer(parking: Orbit, target: Orbit, time: Instant): Double {
    val parkingR = parking.radius(time)
    val targetR = target.radius(time)
    val transferA = (parkingR + targetR) / 2.0
    val mu = parking.parent.gravitationalParameter
    val firstManeuver = abs(visVivaEquation(mu, parkingR, transferA) - visVivaEquation(mu, parkingR, parking.semimajorAxis))
    val secondManeuver = abs(visVivaEquation(mu, targetR, target.semimajorAxis) - visVivaEquation(mu, targetR, transferA))
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
