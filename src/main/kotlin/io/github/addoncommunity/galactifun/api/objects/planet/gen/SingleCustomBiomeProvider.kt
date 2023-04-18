package io.github.addoncommunity.galactifun.api.objects.planet.gen

import io.github.seggan.custombiomeapi.CustomBiome
import org.bukkit.block.Biome
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.WorldInfo

class SingleCustomBiomeProvider(private val biome: CustomBiome) : CustomBiomeProvider() {

    override fun getCustomBiome(x: Int, y: Int, z: Int): CustomBiome = biome

    override fun getAllBiomes(): List<CustomBiome> = listOf(biome)
}