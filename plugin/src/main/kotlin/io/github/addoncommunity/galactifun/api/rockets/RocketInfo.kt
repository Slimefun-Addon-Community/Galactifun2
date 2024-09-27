package io.github.addoncommunity.galactifun.api.rockets

import io.github.addoncommunity.galactifun.EARTH_GRAVITY
import io.github.addoncommunity.galactifun.api.blocks.HeatResistant
import io.github.addoncommunity.galactifun.api.blocks.wetMass
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Gas
import io.github.addoncommunity.galactifun.impl.items.FuelTank
import io.github.addoncommunity.galactifun.impl.items.RocketEngine
import io.github.addoncommunity.galactifun.impl.managers.PlanetManager
import io.github.addoncommunity.galactifun.units.*
import io.github.addoncommunity.galactifun.util.general.mergeMaps
import io.github.addoncommunity.galactifun.util.processSlimefunBlocks
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import me.mrCookieSlime.Slimefun.api.BlockStorage
import kotlin.math.ln
import kotlin.time.Duration.Companion.seconds

class RocketInfo(
    val commandComputer: BlockPosition,
    val blocks: Set<BlockPosition>,
    engineData: Map<Pair<RocketEngine, BlockPosition>, Set<BlockPosition>>
) {

    val engines = engineData.keys.map { it.first }

    val thrust = engines.unitSumOf { it.thrust }
    val wetMass = blocks.unitSumOf { it.block.wetMass }

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

    val dryMass = wetMass - stages.unitSumOf { it.fuelMass }

    val isFullyShielded = blocks.groupBy { it.x to it.z } // organize by column
        .map { (_, v) -> v.maxBy { it.y }} // grab the highest
        .all { BlockStorage.check(it.block) is HeatResistant }

    val info = buildString {
        val planet = PlanetManager.getByWorld(commandComputer.world)
        appendLine("Thrust: %,.2f kilonewtons".format(thrust.kilonewtons))
        appendLine("Wet mass: %.2s".format(wetMass))
        appendLine("Dry mass: %.2s".format(dryMass))
        if (planet != null) {
            appendLine("TWR: %.2f".format(twr(planet.gravity)))
        }
        appendLine("Delta-V: %.2s".format(deltaV(engines, wetMass, dryMass)))
    }

    fun twr(gravity: Acceleration): Double {
        if (gravity == Acceleration.ZERO) return Double.POSITIVE_INFINITY
        if (stages.isEmpty()) return 0.0
        return stages.first().engines.unitSumOf { it.first.thrust } / (wetMass * gravity)
    }

    inner class Stage(
        val engines: List<Pair<RocketEngine, BlockPosition>>,
        val fuelBlocks: Set<BlockPosition>
    ) {
        val fuel: Map<Gas, Volume> = fuelBlocks.processSlimefunBlocks<FuelTank, _>(FuelTank::getFuelLevel)
            .fold(mapOf()) { acc, fuel -> acc.mergeMaps(fuel, Volume::plus) }

        val fuelMass = fuel.toList().unitSumOf { (gas, volume) -> gas.liquidDensity * volume }

        val deltaV = deltaV(
            engines.map { it.first },
            wetMass,
            wetMass - fuelMass
        )
    }
}

private fun deltaV(engines: List<RocketEngine>, wetMass: Mass, dryMass: Mass): Velocity {
    val ispNeum = engines.unitSumOf { it.thrust }.newtons
    val ispDenom = engines.sumOf { it.thrust.newtons / it.specificImpulse.doubleSeconds }
    val isp = if (ispDenom == 0.0) 0.seconds else (ispNeum / ispDenom).seconds

    return EARTH_GRAVITY * isp * ln(wetMass / dryMass)
}
