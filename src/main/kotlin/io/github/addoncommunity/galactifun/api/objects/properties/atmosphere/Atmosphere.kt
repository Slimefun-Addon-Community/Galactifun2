package io.github.addoncommunity.galactifun.api.objects.properties.atmosphere

import io.github.bakedlibs.dough.collections.RandomizedSet
import org.bukkit.GameRule
import org.bukkit.World

class Atmosphere private constructor(
    private val weatherEnabled: Boolean,
    private val storming: Boolean,
    private val thundering: Boolean,
    private val pressure: Double,
    val environment: World.Environment,
    private val composition: Map<Gas, Double>
) {

    private val flammable = composition.getOrDefault(Gas.OXYGEN, 0.0) > 5
    private val growthAttempts = (pressurizedCompositionOf(Gas.CARBON_DIOXIDE) / earthCo2).toInt()

    val weightedCompositionSet = RandomizedSet<Gas>().apply {
        for ((gas, percent) in composition) {
            if (gas.item != null) {
                add(gas, percent.toFloat())
            }
        }
    }

    fun compositionOf(gas: Gas): Double {
        return composition.getOrDefault(gas, 0.0)
    }

    fun pressurizedCompositionOf(gas: Gas): Double {
        return compositionOf(gas) * pressure
    }

    fun applyEffects(world: World) {
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, weatherEnabled)
        world.setStorm(storming)
        if (storming) {
            world.weatherDuration = Int.MAX_VALUE
        }
        world.isThundering = thundering
        if (thundering) {
            world.thunderDuration = Int.MAX_VALUE
        }
        world.setGameRule(GameRule.DO_FIRE_TICK, flammable)
    }

    companion object {
        fun buildAtmosphere(builder: AtmosphereBuilder.() -> Unit): Atmosphere {
            val atmosphereBuilder = AtmosphereBuilder()
            builder(atmosphereBuilder)
            return Atmosphere(
                atmosphereBuilder.weatherEnabled,
                atmosphereBuilder.storming,
                atmosphereBuilder.thundering,
                atmosphereBuilder.pressure,
                atmosphereBuilder.environment,
                atmosphereBuilder.composition
            )
        }
    }
}

private const val earthCo2 = 0.0415