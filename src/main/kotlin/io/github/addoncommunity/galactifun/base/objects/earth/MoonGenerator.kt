package io.github.addoncommunity.galactifun.base.objects.earth

import io.github.addoncommunity.galactifun.api.objects.planet.gen.CustomBiomeProvider
import io.github.addoncommunity.galactifun.api.objects.planet.gen.WorldGenerator
import io.github.addoncommunity.galactifun.util.buildCustomBiome
import io.github.addoncommunity.galactifun.util.gen.DoubleChunkGrid
import io.github.addoncommunity.galactifun.util.toKey
import io.github.seggan.custombiomeapi.CustomBiome
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.generator.WorldInfo
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.util.Random
import kotlin.math.pow

internal class MoonGenerator : WorldGenerator() {

    override val biomeProvider = MoonBiomeProvider()

    @Volatile
    private lateinit var baseNoise: SimplexOctaveGenerator

    @Volatile
    private lateinit var heightNoise: SimplexOctaveGenerator

    private val baseNoiseGrid = DoubleChunkGrid(256 * 2)
    private val heightNoiseGrid = DoubleChunkGrid(256 * 3)

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
                if (terrainType == TerrainType.MARE) {
                    for (y in height - 2 until height) {
                        chunkData.setBlock(x, y, z, Material.GRAY_CONCRETE)
                    }
                } else {
                    chunkData.setBlock(x, height - 2, z, Material.GRAY_CONCRETE_POWDER)
                    chunkData.setBlock(
                        x, height - 1, z, when (terrainType) {
                            TerrainType.TRANSITION_25 -> if (random.nextDouble() < 0.25) Material.GRAY_CONCRETE_POWDER else Material.LIGHT_GRAY_CONCRETE_POWDER
                            TerrainType.TRANSITION_50 -> if (random.nextBoolean()) Material.GRAY_CONCRETE_POWDER else Material.LIGHT_GRAY_CONCRETE_POWDER
                            TerrainType.TRANSITION_75 -> if (random.nextDouble() < 0.75) Material.GRAY_CONCRETE_POWDER else Material.LIGHT_GRAY_CONCRETE_POWDER
                            else -> Material.LIGHT_GRAY_CONCRETE_POWDER
                        }
                    )
                }
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
            height < 3.5 -> TerrainType.HILLS
            height < 3.75 -> TerrainType.TRANSITION_25
            height < 4.0 -> TerrainType.TRANSITION_50
            height < 4.25 -> TerrainType.TRANSITION_75
            else -> TerrainType.MARE
        }
    }

    inner class MoonBiomeProvider : CustomBiomeProvider() {
        override fun getCustomBiome(worldInfo: WorldInfo, x: Int, y: Int, z: Int): CustomBiome =
            if (getTerrainType(worldInfo, x, z) == TerrainType.MARE) lunarMare else lunarHills

        override fun getAllBiomes(): List<CustomBiome> = listOf(lunarHills, lunarMare)
    }
}

private enum class TerrainType {
    HILLS,
    TRANSITION_25,
    TRANSITION_50,
    TRANSITION_75,
    MARE
}

private val lunarHills = buildCustomBiome("lunar_hills".toKey(), Biome.WINDSWEPT_HILLS) {
    skyColor = Color.BLACK
    fogColor = Color.BLACK
}

private val lunarMare = buildCustomBiome("lunar_mare".toKey(), Biome.DEEP_OCEAN) {
    skyColor = Color.BLACK
    fogColor = Color.BLACK
}