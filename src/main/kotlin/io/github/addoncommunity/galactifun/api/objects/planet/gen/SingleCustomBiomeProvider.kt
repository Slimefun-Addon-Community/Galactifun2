package io.github.addoncommunity.galactifun.api.objects.planet.gen

import io.github.seggan.custombiomeapi.CustomBiome
import org.bukkit.generator.WorldInfo

class SingleCustomBiomeProvider(private val biome: CustomBiome) : CustomBiomeProvider() {

    override fun getCustomBiome(worldInfo: WorldInfo, x: Int, y: Int, z: Int): CustomBiome = biome

    override fun getAllBiomes(): List<CustomBiome> = listOf(biome)
}