package io.github.addoncommunity.galactifun.api.objects.planet.gen

import org.bukkit.World
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo

/**
 * A world generator for an [AlienWorld], plus a [BiomeProvider] and a [BlockPopulator]
 */
abstract class WorldGenerator : ChunkGenerator() {
    abstract val biomeProvider: BiomeProvider
    protected val populators = mutableListOf<BlockPopulator>()

    final override fun getDefaultPopulators(world: World): MutableList<BlockPopulator> = populators

    final override fun getDefaultBiomeProvider(worldInfo: WorldInfo): BiomeProvider = biomeProvider
}