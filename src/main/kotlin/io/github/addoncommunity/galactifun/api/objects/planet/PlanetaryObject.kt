package io.github.addoncommunity.galactifun.api.objects.planet

import io.github.addoncommunity.galactifun.api.objects.UniversalObject
import io.github.addoncommunity.galactifun.api.objects.properties.DayCycle
import io.github.addoncommunity.galactifun.api.objects.properties.OrbitPosition
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Atmosphere
import io.github.addoncommunity.galactifun.core.managers.PlanetManager
import io.github.addoncommunity.galactifun.util.Constants
import io.github.addoncommunity.galactifun.util.units.Distance
import io.github.seggan.kfun.location.plus
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import kotlin.math.abs
import kotlin.math.sqrt

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
        if (other in generateSequence(this) { it.orbiting as? PlanetaryObject }) {
            return other.getDeltaVForTransferTo(this)
        }
        val otherParents = generateSequence(other) { it.orbiting as? PlanetaryObject }
        if (this in otherParents) {
            var height = other.parkingOrbit
            var dV = 0.0
            for (obj in otherParents) {
                if (obj == this) break
                val mu = obj.mass.kilograms * Constants.GRAVITATIONAL_CONSTANT
                dV += abs(obj.escapeVelocity - visViva(mu, height, height))
                height = obj.orbit.distance
            }
            dV += hohmannTransfer(mass.kilograms * Constants.GRAVITATIONAL_CONSTANT, height, parkingOrbit)
            return dV
        } else {
            TODO("This is a sibling object")
        }
    }
}

private fun visViva(mu: Double, r: Distance, a: Distance): Double =
    sqrt(mu * (2 / r.kilometers - 1 / a.kilometers))

private fun hohmannTransfer(mu: Double, parkingR: Distance, targetR: Distance): Double {
    val transferA = (parkingR + targetR) / 2
    val firstManeuver = abs(visViva(mu, parkingR, transferA) - visViva(mu, parkingR, parkingR))
    val secondManeuver = abs(visViva(mu, targetR, targetR) - visViva(mu, targetR, transferA))
    return firstManeuver + secondManeuver
}