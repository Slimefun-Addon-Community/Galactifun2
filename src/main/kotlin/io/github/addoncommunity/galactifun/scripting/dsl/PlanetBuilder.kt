package io.github.addoncommunity.galactifun.scripting.dsl

import io.github.addoncommunity.galactifun.api.objects.UniversalObject
import io.github.addoncommunity.galactifun.api.objects.planet.PlanetaryObject
import io.github.addoncommunity.galactifun.api.objects.planet.PlanetaryWorld
import io.github.addoncommunity.galactifun.api.objects.properties.DayCycle
import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.scripting.PlanetDsl
import io.github.addoncommunity.galactifun.scripting.RequiredProperty
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.time.Duration

@PlanetDsl
class PlanetBuilder {

    var name: String by RequiredProperty()
    var item: Material by RequiredProperty()
    var orbiting: UniversalObject by RequiredProperty()
    var orbit: Orbit by RequiredProperty()
    var dayCycle: DayCycle by RequiredProperty()

    fun build(): PlanetaryObject {
        return object : PlanetaryObject(name, ItemStack(item)) {
            override val dayCycle = this@PlanetBuilder.dayCycle
            override val orbiting = this@PlanetBuilder.orbiting
            override val orbit = this@PlanetBuilder.orbit
        }
    }

    val Duration.long: DayCycle
        get() = DayCycle(this)
}

inline fun planet(block: PlanetBuilder.() -> Unit): PlanetaryObject {
    val planet = PlanetBuilder().apply(block).build()
    if (planet is PlanetaryWorld) {
        planet.register()
    }
    return planet
}

inline fun PlanetBuilder.orbit(block: OrbitBuilder.() -> Unit) {
    orbit = OrbitBuilder().apply(block).build()
}

fun PlanetBuilder.eternal(ticks: Int): DayCycle = DayCycle.eternal(ticks)