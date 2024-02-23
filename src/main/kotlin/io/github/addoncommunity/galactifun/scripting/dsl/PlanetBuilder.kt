package io.github.addoncommunity.galactifun.scripting.dsl

import io.github.addoncommunity.galactifun.api.objects.CelestialObject
import io.github.addoncommunity.galactifun.api.objects.PlanetaryObject
import io.github.addoncommunity.galactifun.api.objects.planet.AlienWorld
import io.github.addoncommunity.galactifun.api.objects.planet.PlanetaryWorld
import io.github.addoncommunity.galactifun.api.objects.properties.DayCycle
import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Atmosphere
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.AtmosphereBuilder
import io.github.addoncommunity.galactifun.scripting.PlanetDsl
import io.github.addoncommunity.galactifun.scripting.PlanetScript
import io.github.addoncommunity.galactifun.units.Distance
import io.github.addoncommunity.galactifun.units.Mass
import io.github.addoncommunity.galactifun.util.RequiredProperty
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.inventory.ItemStack
import kotlin.time.Duration

@PlanetDsl
class PlanetBuilder {

    var name: String by RequiredProperty()
    var item: Material by RequiredProperty()
    var orbiting: CelestialObject by RequiredProperty()
    var orbit: Orbit by RequiredProperty()
    var mass: Mass by RequiredProperty()
    var radius: Distance by RequiredProperty()
    var dayCycle: DayCycle by RequiredProperty()

    var atmosphere = Atmosphere.NONE

    var worldConfig: WorldBuilder? = null

    fun build(): PlanetaryObject {
        val config = worldConfig
        if (config != null) {
            val world = config.world
            if (world != null) {
                return object : PlanetaryWorld(name, ItemStack(item)) {
                    override val dayCycle = this@PlanetBuilder.dayCycle
                    override val orbit = this@PlanetBuilder.orbit
                    override val mass = this@PlanetBuilder.mass
                    override val radius = this@PlanetBuilder.radius
                    override val atmosphere = this@PlanetBuilder.atmosphere

                    override fun loadWorld(): World = Bukkit.getWorld(world) ?: error("World $world does not exist")
                }
            } else {
                return object : AlienWorld(name, ItemStack(item)) {
                    override val dayCycle = this@PlanetBuilder.dayCycle
                    override val orbit = this@PlanetBuilder.orbit
                    override val mass = this@PlanetBuilder.mass
                    override val radius = this@PlanetBuilder.radius
                    override val atmosphere = this@PlanetBuilder.atmosphere
                    override val generator = config.generator

                    init {
                        for ((material, item) in config.blockMappings) {
                            addBlockMapping(material, item)
                        }
                    }
                }
            }
        } else {
            return object : PlanetaryObject(name, ItemStack(item)) {
                override val dayCycle = this@PlanetBuilder.dayCycle
                override val orbit = this@PlanetBuilder.orbit
                override val mass = this@PlanetBuilder.mass
                override val radius = this@PlanetBuilder.radius
                override val atmosphere = this@PlanetBuilder.atmosphere
            }
        }
    }

    val Duration.long: DayCycle
        get() = DayCycle(this)
}

inline fun PlanetScript.planet(block: PlanetBuilder.() -> Unit): PlanetaryObject {
    val planet = PlanetBuilder().apply(block).build()
    toRegister.add(planet)
    return planet
}

fun PlanetBuilder.eternal(ticks: Int): DayCycle = DayCycle.eternal(ticks)

fun PlanetBuilder.atmosphere(block: AtmosphereBuilder.() -> Unit) {
    atmosphere = Atmosphere.buildAtmosphere(block)
}

fun PlanetBuilder.world(block: WorldBuilder.() -> Unit) {
    worldConfig = WorldBuilder().apply(block)
}