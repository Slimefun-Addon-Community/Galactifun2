package io.github.addoncommunity.galactifun.api.rockets

import io.github.addoncommunity.galactifun.Constants
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Gas
import io.github.addoncommunity.galactifun.impl.items.FuelTank
import io.github.addoncommunity.galactifun.impl.items.RocketEngine
import io.github.addoncommunity.galactifun.impl.managers.PlanetManager
import io.github.addoncommunity.galactifun.units.*
import io.github.addoncommunity.galactifun.util.general.mergeMaps
import io.github.addoncommunity.galactifun.util.items.wetMass
import io.github.addoncommunity.galactifun.util.processSlimefunBlocks
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import kotlin.math.ln
import kotlin.time.Duration.Companion.seconds

class RocketInfo(
    val commandComputer: BlockPosition,
    val blocks: Set<BlockPosition>,
    engineData: Map<Pair<RocketEngine, BlockPosition>, Set<BlockPosition>>
) {

    val engines = engineData.keys.map { it.first }

    val thrust = engines.sumBy { it.thrust }
    val wetMass = blocks.sumBy { it.block.wetMass }

    val stages: List<Stage>

    init {
        stages = if (engineData.isEmpty()) {
            emptyList()
        } else {
            val sortedEngines = engineData.toList().sortedBy { it.first.second.y }
            val stageList = mutableListOf<Stage>()

            for ((engine, fuelBlocks) in sortedEngines) {
                val engines = stageList.flatMap { it.engines }.map { it.second }
                if (stageList.any { engine.second in engines }) continue
                val stage = sortedEngines.filter { it.second == fuelBlocks }.map { it.first }
                stageList.add(Stage(stage, fuelBlocks))
            }

            stageList
        }
    }

    val dryMass = wetMass - stages.sumBy { it.fuelMass }

    val info = buildString {
        val planet = PlanetManager.getByWorld(commandComputer.world) ?: error("Planet not found")
        appendLine("Stages:")
        var stageNum = 1
        for (stage in stages) {
            appendLine("  Stage ${stageNum++}: ")
            appendLine("    Fuel:")
            for ((gas, volume) in stage.fuel) {
                appendLine("      $gas: %.2s, %.2s".format(volume, volume * gas.liquidDensity))
            }
            appendLine("    Engines:")
            var engineNum = 1
            for (engine in stage.engines) {
                appendLine("      Engine ${engineNum++}: %.2f kilonewtons".format(engine.first.thrust.kilonewtons))
            }
            appendLine("    Delta-V: %.2f m/s".format(stage.deltaV.metersPerSecond))
        }
        appendLine("Thrust: %.2f kilonewtons".format(thrust.kilonewtons))
        appendLine("Wet mass: %.2s".format(wetMass))
        appendLine("Dry mass: %.2s".format(dryMass))
        appendLine("TWR: %.2f".format(twr(planet.gravity)))
        appendLine("Delta-V: %.2f m/s".format(deltaV(engines, wetMass, dryMass).metersPerSecond))
    }

    fun twr(gravity: Acceleration): Double {
        if (gravity == Acceleration.ZERO) return Double.POSITIVE_INFINITY
        return thrust / (wetMass * gravity)
    }

    inner class Stage(
        val engines: List<Pair<RocketEngine, BlockPosition>>,
        val fuelBlocks: Set<BlockPosition>
    ) {
        val fuel: Map<Gas, Volume> = fuelBlocks.processSlimefunBlocks<FuelTank, _>(FuelTank::getFuelLevel)
            .fold(mapOf()) { acc, fuel -> acc.mergeMaps(fuel, Volume::plus) }

        val fuelMass = fuel.toList().sumBy { (gas, volume) -> gas.liquidDensity * volume }

        val deltaV = deltaV(
            engines.map { it.first },
            wetMass,
            wetMass - fuelMass
        )
    }
}

private fun deltaV(engines: List<RocketEngine>, wetMass: Mass, dryMass: Mass): Velocity {
    val ispNeum = engines.sumBy { it.thrust }.newtons
    val ispDenom = engines.sumOf { it.thrust.newtons / it.specificImpulse.doubleSeconds }
    val isp = (ispNeum / ispDenom).seconds

    return Constants.EARTH_GRAVITY * isp * ln(wetMass / dryMass)
}
