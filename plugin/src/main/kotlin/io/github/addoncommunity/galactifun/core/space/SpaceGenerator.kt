package io.github.addoncommunity.galactifun.core.space

import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo

internal object SpaceGenerator : ChunkGenerator() {
    override fun getDefaultBiomeProvider(worldInfo: WorldInfo) = object : BiomeProvider() {
        override fun getBiome(worldInfo: WorldInfo, x: Int, y: Int, z: Int)= Biome.THE_END
        override fun getBiomes(worldInfo: WorldInfo) = mutableListOf(Biome.THE_END)
    }

    override fun getDefaultPopulators(world: World): MutableList<BlockPopulator> =
        mutableListOf(SpacePopulator)
}