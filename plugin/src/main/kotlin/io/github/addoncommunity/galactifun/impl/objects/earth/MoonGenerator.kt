package io.github.addoncommunity.galactifun.impl.objects.earth

import io.github.addoncommunity.galactifun.api.objects.planet.gen.WorldGenerator
import io.github.addoncommunity.galactifun.util.worldgen.DoubleChunkGrid
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.WorldInfo
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.util.*
import kotlin.math.pow

internal class MoonGenerator : WorldGenerator() {

    override val biomeProvider: BiomeProvider = MoonBiomeProvider()

    @Volatile
    private lateinit var baseNoise: SimplexOctaveGenerator

    @Volatile
    private lateinit var heightNoise: SimplexOctaveGenerator

    private val baseNoiseGrid = DoubleChunkGrid()
    private val heightNoiseGrid = DoubleChunkGrid()

    override fun generateBedrock(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {
        for (x in 0..15) {
            for (z in 0..15) {
                chunkData.setBlock(x, worldInfo.minHeight, z, Material.BEDROCK)
            }
        }
    }

    override fun generateNoise(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {
        val cx = chunkX * 16
        val cz = chunkZ * 16
        val min = worldInfo.minHeight + 1
        for (x in 0..15) {
            for (z in 0..15) {
                val height = getHeight(worldInfo, cx + x, cz + z)
                for (y in min until height - 2) {
                    chunkData.setBlock(x, y, z, Material.STONE)
                }
            }
        }
    }

    override fun generateSurface(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {
        val cx = chunkX * 16
        val cz = chunkZ * 16
        for (x in 0..15) {
            for (z in 0..15) {
                val height = getHeight(worldInfo, cx + x, cz + z)
                val terrainType = getTerrainType(worldInfo, cx + x, cz + z)
                chunkData.setBlock(x, height - 2, z, Material.GRAY_CONCRETE_POWDER)
                chunkData.setBlock(
                    x,
                    height - 1,
                    z,
                    when (terrainType) {
                        is TerrainType.Mare -> Material.GRAY_CONCRETE_POWDER

                        is TerrainType.Transition ->
                            if (random.nextDouble() < terrainType.chance) Material.GRAY_CONCRETE_POWDER
                            else Material.LIGHT_GRAY_CONCRETE_POWDER

                        is TerrainType.Hills -> Material.LIGHT_GRAY_CONCRETE_POWDER
                    }
                )
            }
        }
    }

    private fun getHeight(worldInfo: WorldInfo, x: Int, y: Int): Int {
        if (!::baseNoise.isInitialized) {
            baseNoise = SimplexOctaveGenerator(worldInfo.seed, 8)
            baseNoise.setScale(1 / 64.0)
        }

        var base = baseNoiseGrid.getOrSet(x, y) {
            (baseNoise.noise(x.toDouble(), y.toDouble(), 0.5, 0.5, true) + 1) / 2
        }
        base = base.pow(getHeightValue(worldInfo, x, y))

        return (base * 40 - worldInfo.minHeight).toInt()
    }

    private fun getHeightValue(worldInfo: WorldInfo, x: Int, y: Int): Double {
        if (!::heightNoise.isInitialized) {
            heightNoise = SimplexOctaveGenerator(worldInfo.seed, 8)
            heightNoise.setScale(1 / 1024.0)
        }

        val height = heightNoiseGrid.getOrSet(x, y) {
            (heightNoise.noise(x.toDouble(), y.toDouble(), 0.5, 0.5, true) + 1) / 2
        }
        return height * 4 + 1
    }

    private fun getTerrainType(worldInfo: WorldInfo, x: Int, y: Int): TerrainType {
        val height = getHeightValue(worldInfo, x, y)
        return when {
            height < 3 -> TerrainType.Hills
            height < 3.5 -> TerrainType.Transition((height - 3) / 0.5)
            else -> TerrainType.Mare
        }
    }

    private inner class MoonBiomeProvider : BiomeProvider() {
        override fun getBiome(worldInfo: WorldInfo, x: Int, y: Int, z: Int): Biome {
            return if (getTerrainType(worldInfo, x, z) == TerrainType.Mare) Biome.BADLANDS else Biome.DESERT
        }

        override fun getBiomes(worldInfo: WorldInfo): MutableList<Biome> = mutableListOf(Biome.DESERT, Biome.BADLANDS)
    }
}

private sealed interface TerrainType {
    data object Hills : TerrainType
    data class Transition(val chance: Double) : TerrainType
    data object Mare : TerrainType
}