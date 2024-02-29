package io.github.addoncommunity.galactifun.api.objects

import io.github.addoncommunity.galactifun.api.objects.properties.DayCycle
import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.api.objects.properties.OrbitPosition
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Atmosphere
import io.github.addoncommunity.galactifun.api.objects.properties.visVivaEquation
import io.github.addoncommunity.galactifun.core.managers.PlanetManager
import io.github.addoncommunity.galactifun.units.Distance
import io.github.addoncommunity.galactifun.units.Distance.Companion.meters
import io.github.addoncommunity.galactifun.units.Velocity
import io.github.addoncommunity.galactifun.units.Velocity.Companion.metersPerSecond
import io.github.addoncommunity.galactifun.units.abs
import io.github.addoncommunity.galactifun.units.cos
import io.github.seggan.kfun.location.plus
import kotlinx.datetime.Instant
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import kotlin.math.sqrt

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
        if (other == this) return 0.meters
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

    fun getDeltaVForTransferTo(other: PlanetaryObject, time: Instant): Velocity {
        if (this == other) return 0.metersPerSecond
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
            var dV = 0.metersPerSecond
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
            dV += height.hohmannTransfer(parkingOrbit, time)
            return dV
        } else {
            val commonParent = thisParents.firstOrNull { it in otherParents }
                ?: return Double.POSITIVE_INFINITY.metersPerSecond
            val thisSibling = thisParents[thisParents.indexOf(commonParent) - 1] as PlanetaryObject
            val otherSibling = otherParents[otherParents.indexOf(commonParent) - 1] as PlanetaryObject
            val thisDeltaV = abs(
                thisSibling.escapeVelocity - visVivaEquation(
                    thisSibling.gravitationalParameter,
                    thisSibling.parkingOrbit.radius(time),
                    thisSibling.parkingOrbit.semimajorAxis
                )
            ) + getDeltaVForTransferTo(thisSibling, time)
            val otherDeltaV = abs(
                otherSibling.escapeVelocity - visVivaEquation(
                    otherSibling.gravitationalParameter,
                    otherSibling.parkingOrbit.radius(time),
                    otherSibling.parkingOrbit.semimajorAxis
                )
            ) + other.getDeltaVForTransferTo(otherSibling, time)
            return thisSibling.orbit.arbitraryTransfer(otherSibling.orbit, time) + thisDeltaV + otherDeltaV
        }
    }
}
