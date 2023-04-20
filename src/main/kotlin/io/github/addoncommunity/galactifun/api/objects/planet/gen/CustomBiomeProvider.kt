package io.github.addoncommunity.galactifun.api.objects.planet.gen

import io.github.seggan.custombiomeapi.CustomBiome
import org.bukkit.block.Biome
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.WorldInfo

abstract class CustomBiomeProvider : BiomeProvider() {

    final override fun getBiome(worldInfo: WorldInfo, x: Int, y: Int, z: Int): Biome =
        getCustomBiome(worldInfo, x, y, z).baseBiome()

    final override fun getBiomes(worldInfo: WorldInfo): MutableList<Biome> =
        getAllBiomes().mapTo(mutableListOf(), CustomBiome::baseBiome)

    abstract fun getCustomBiome(worldInfo: WorldInfo, x: Int, y: Int, z: Int): CustomBiome

    abstract fun getAllBiomes(): List<CustomBiome>

}