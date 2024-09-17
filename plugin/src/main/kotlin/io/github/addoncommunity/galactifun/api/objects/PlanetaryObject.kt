package io.github.addoncommunity.galactifun.api.objects

import io.github.addoncommunity.galactifun.TAU
import io.github.addoncommunity.galactifun.api.objects.properties.DayCycle
import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.api.objects.properties.OrbitPosition
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Atmosphere
import io.github.addoncommunity.galactifun.api.objects.properties.visVivaEquation
import io.github.addoncommunity.galactifun.impl.managers.PlanetManager
import io.github.addoncommunity.galactifun.units.*
import io.github.addoncommunity.galactifun.units.Distance.Companion.meters
import io.github.addoncommunity.galactifun.units.Velocity.Companion.metersPerSecond
import kotlinx.datetime.Instant
import org.bukkit.inventory.ItemStack
import kotlin.math.sqrt
import kotlin.time.Duration

abstract class PlanetaryObject(name: String, baseItem: ItemStack) : CelestialObject(name, baseItem) {

    abstract val dayCycle: DayCycle
    abstract val atmosphere: Atmosphere
    abstract val orbit: Orbit

    val surfaceToOrbitCost by lazy {
        visVivaEquation(
            gravitationalParameter,
            parkingOrbit.semimajorAxis,
            parkingOrbit.semimajorAxis
        ) - (radius.meters * TAU / dayCycle.duration.doubleSeconds).metersPerSecond // surface velocity
    }

    val star: Star by lazy {
        when (val parent = orbit.parent) {
            is Star -> parent
            is PlanetaryObject -> parent.star
        }
    }

    val orbitPosition: OrbitPosition
        get() = PlanetManager.getOrbit(this)

    override fun distanceTo(other: CelestialObject, time: Instant): Distance {
        if (other == this) return 0.meters
        when (other) {
            is Star -> if (star == other) {
                var dist = orbit.radius(time)
                if (orbit.parent != other) {
                    dist += orbit.parent.distanceTo(other, time)
                }
                return dist
            } else {
                return star.distanceTo(other, time) + distanceTo(star, time)
            }

            is PlanetaryObject -> if (star == other.star) {
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

    override fun addLoreProperties(lore: MutableList<String>) {
        super.addLoreProperties(lore)
        lore.add("")
        lore.add("Day length: $dayCycle")
        lore.add("")
        lore.add("Semimajor axis: %,d kilometers".format(orbit.semimajorAxis.kilometers))
        val c = orbit.semimajorAxis * orbit.eccentricity
        lore.add("Periapasis: %,d kilometers".format((orbit.semimajorAxis - c).kilometers))
        lore.add("Apoapsis: %,d kilometers".format((orbit.semimajorAxis + c).kilometers))
        lore.add("Eccentricity: %.2f".format(orbit.eccentricity))
        lore.add("Longitude of periapsis: %.2f°".format(orbit.longitudeOfPeriapsis.degrees))
        lore.add("Orbital period (year length): ${durationToYearsAndDays(orbit.period)}")
        if (atmosphere.pressure > 0) {
            lore.add("")
            lore.add("Atmospheric pressure: %.2f atmospheres".format(atmosphere.pressure))
        }
        if (orbiters.isNotEmpty()) {
            lore.add("")
            lore.add("Number of moons: ${orbiters.size}")
        }
    }
}

private fun durationToYearsAndDays(duration: Duration): String {
    val sb = StringBuilder()
    val years = duration.inWholeDays / 365
    val days = duration.inWholeDays % 365
    if (years > 0) {
        sb.append(years)
        sb.append(" year")
        if (years > 1) sb.append('s')
    }
    if (days > 0) {
        if (years > 0) sb.append(", ")
        sb.append(days)
        sb.append(" day")
        if (days > 1) sb.append('s')
    }
    return sb.toString()
}
