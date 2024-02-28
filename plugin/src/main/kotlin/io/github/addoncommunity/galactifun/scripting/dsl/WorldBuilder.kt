package io.github.addoncommunity.galactifun.scripting.dsl

import io.github.addoncommunity.galactifun.api.objects.planet.gen.WorldGenerator
import io.github.addoncommunity.galactifun.scripting.PlanetDsl
import io.github.addoncommunity.galactifun.scripting.dsl.gen.GeneratorBuilder
import io.github.addoncommunity.galactifun.scripting.dsl.gen.GeneratorBuilderProvider
import io.github.addoncommunity.galactifun.util.RequiredProperty
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*

@PlanetDsl
class WorldBuilder {

    var world: String? = null

    var generator: WorldGenerator by RequiredProperty()

    var spawnVanillaMobs = false

    internal val blockMappings = EnumMap<Material, ItemStack>(Material::class.java)

    @PlanetDsl
    inner class MappingBuilder {
        infix fun Material.mapTo(item: ItemStack) {
            this@WorldBuilder.blockMappings[this] = item
        }

        infix fun Material.mapTo(item: Material) {
            this mapTo ItemStack(item)
        }
    }
}

inline fun WorldBuilder.blockMappings(block: WorldBuilder.MappingBuilder.() -> Unit) {
    MappingBuilder().apply(block)
}

inline fun <T : GeneratorBuilder> WorldBuilder.generator(
    provider: GeneratorBuilderProvider<T>,
    block: T.() -> Unit
) {
    generator = provider.provide().apply(block).build()
}