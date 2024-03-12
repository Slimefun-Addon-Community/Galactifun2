package io.github.addoncommunity.galactifun.api.rockets

import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Gas
import io.github.addoncommunity.galactifun.impl.items.RocketEngine
import io.github.addoncommunity.galactifun.impl.managers.PlanetManager
import io.github.addoncommunity.galactifun.units.Acceleration
import io.github.addoncommunity.galactifun.units.Force
import io.github.addoncommunity.galactifun.units.Mass
import io.github.addoncommunity.galactifun.units.times
import io.github.addoncommunity.galactifun.util.items.mass
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition

data class RocketInfo(
    val commandComputer: BlockPosition,
    val blocks: Set<BlockPosition>,
    val engines: List<Pair<RocketEngine, Set<BlockPosition>>>
) {
    val thrust: Force
        get() = engines.fold(Force.ZERO) { acc, engine -> acc + engine.first.thrust }
    val mass: Mass
        get() = blocks.fold(Mass.ZERO) { acc, block -> acc + block.block.mass }

    fun twr(gravity: Acceleration): Double {
        if (gravity == Acceleration.ZERO) return Double.POSITIVE_INFINITY
        return thrust / (mass * gravity)
    }

    val fuel: List<Pair<List<RocketEngine>, Map<Gas, Double>>>
        get() {
            TODO()
        }

    val info: String
        get() = buildString {
            val planet = PlanetManager.getByWorld(commandComputer.world) ?: error("Planet not found")
            appendLine("Fuel:")
            for ((gas, amount) in fuel) {
                append(" ".repeat(4))
                append(gas)
                //appendLine(": %.2f l, %.2f kg".format(amount, amount * gas.liquidDensity))
            }
            appendLine("Thrust: %.2f kN".format(thrust.kilonewtons))
            appendLine("Mass: %.2f kg".format(mass.kilograms))
            appendLine("TWR: %.2f".format(twr(planet.gravity)))
        }
}
