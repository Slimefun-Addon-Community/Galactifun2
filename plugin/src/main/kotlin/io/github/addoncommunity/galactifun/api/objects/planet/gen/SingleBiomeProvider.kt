package io.github.addoncommunity.galactifun.api.objects.planet.gen

import org.bukkit.block.Biome
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.WorldInfo

class SingleBiomeProvider(private val biome: Biome) : BiomeProvider() {

    override fun getBiome(worldInfo: WorldInfo, x: Int, y: Int, z: Int): Biome = biome

    override fun getBiomes(worldInfo: WorldInfo): MutableList<Biome> = mutableListOf(biome)
}