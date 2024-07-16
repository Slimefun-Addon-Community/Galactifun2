package io.github.addoncommunity.galactifun.api.objects.properties.atmosphere

import io.github.addoncommunity.galactifun.util.bukkit.set
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.RandomizedSet
import org.bukkit.GameRule
import org.bukkit.Material
import org.bukkit.World

class Atmosphere private constructor(
    private val weatherEnabled: Boolean,
    private val storming: Boolean,
    private val thundering: Boolean,
    val pressure: Double,
    private val composition: Map<Gas, Double>
) {

    val environment = when {
        pressure > 2.0 -> World.Environment.NETHER
        pressure < 0.001 -> World.Environment.THE_END
        else -> World.Environment.NORMAL
    }

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

        if (world.environment == World.Environment.THE_END) {
            // Prevents ender dragon spawn using portal, surrounds portal with bedrock
            world[0, 1, 0] = Material.END_PORTAL
            world[0, 2, 0] = Material.BEDROCK
            world[0, 0, 0] = Material.BEDROCK
            world[1, 1, 0] = Material.BEDROCK
            world[-1, 1, 0] = Material.BEDROCK
            world[0, 1, 1] = Material.BEDROCK
            world[0, 1, -1] = Material.BEDROCK
        }
    }

    companion object {
        fun buildAtmosphere(builder: AtmosphereBuilder.() -> Unit): Atmosphere {
            val atmosphereBuilder = AtmosphereBuilder()
            builder(atmosphereBuilder)

            val sum = atmosphereBuilder.composition.values.sum()
            require(sum < 101 && sum >= 0) { "Atmosphere composition cannot be greater than 101%, was $sum%" }
            if (sum > 0) {
                atmosphereBuilder.composition[Gas.OTHER] =
                    atmosphereBuilder.composition.getOrDefault(Gas.OTHER, 0.0) + (100 - sum)
            }

            return Atmosphere(
                atmosphereBuilder.weatherEnabled,
                atmosphereBuilder.storming,
                atmosphereBuilder.thundering,
                atmosphereBuilder.pressure,
                atmosphereBuilder.composition
            )
        }

        val EARTH_LIKE = buildAtmosphere {
            weatherEnabled = true
            pressure = 1.0

            composition {
                77.084 percent Gas.NITROGEN // subtracted 1 to allow water to fit in
                20.946 percent Gas.OXYGEN
                0.95 percent Gas.WATER
                0.934 percent Gas.ARGON
                earthCo2 percent Gas.CARBON_DIOXIDE
            }
        }

        val NONE = buildAtmosphere {
            weatherEnabled = false
            pressure = 0.0
        }
    }
}

private const val earthCo2 = 0.0415