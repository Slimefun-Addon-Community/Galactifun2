package io.github.addoncommunity.galactifun.scripting.dsl.gen

import io.github.addoncommunity.galactifun.api.objects.planet.gen.WorldGenerator
import io.github.addoncommunity.galactifun.util.general.RequiredProperty
import org.bukkit.block.Biome
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.WorldInfo

abstract class GeneratorBuilder {

    var biomeProvider: BiomeProvider by RequiredProperty()

    abstract fun build(): WorldGenerator
}

fun GeneratorBuilder.singleBiome(biome: Biome) {
    biomeProvider = object : BiomeProvider() {
        override fun getBiome(worldInfo: WorldInfo, x: Int, y: Int, z: Int) = biome
        override fun getBiomes(worldInfo: WorldInfo) = mutableListOf(biome)
    }
}

interface GeneratorBuilderProvider<T : GeneratorBuilder> {
    fun provide(): T
}
