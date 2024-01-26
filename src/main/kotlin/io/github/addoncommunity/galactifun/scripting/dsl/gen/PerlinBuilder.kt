package io.github.addoncommunity.galactifun.scripting.dsl.gen

import io.github.addoncommunity.galactifun.api.objects.planet.gen.WorldGenerator
import org.bukkit.Material
import org.bukkit.generator.WorldInfo
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.util.*
import kotlin.math.pow

class PerlinBuilder : AbstractPerlin() {

    var noiseGenerator: (WorldInfo, Random, Int, Int, Int, Int) -> Material =
        { _, _, _, _, _, _ -> Material.AIR }

    var surfaceGenerator: (WorldInfo, Random, Int, Int, Int, Int) -> Material =
        { _, _, _, _, _, _ -> Material.AIR }


    override fun build(): WorldGenerator {
        // Prevent performance issues by unboxing before building the generator
        val octaves = config.octaves
        val scale = config.scale
        val amplitude = config.amplitude
        val frequency = config.frequency
        val flattenFactor = config.flattenFactor
        val smoothen = config.smoothen

        val averageHeight = averageHeight
        val minHeight = minHeight + if (generateBedrock) 1 else 0

        return object : WorldGenerator() {
            override val biomeProvider = this@PerlinBuilder.biomeProvider

            @Volatile
            private lateinit var baseNoise: SimplexOctaveGenerator

            fun getMinHeight(worldInfo: WorldInfo): Int = minHeight.coerceAtLeast(worldInfo.minHeight)

            override fun generateNoise(
                worldInfo: WorldInfo,
                random: Random,
                chunkX: Int,
                chunkZ: Int,
                chunkData: ChunkData
            ) {
                val cx = chunkX * 16
                val cz = chunkZ * 16
                val min = getMinHeight(worldInfo)
                for (x in 0..15) {
                    for (z in 0..15) {
                        val height = getHeight(worldInfo, cx + x, cz + z)
                        for (y in min until height - surfaceHeight) {
                            chunkData.setBlock(x, y, z, noiseGenerator(worldInfo, random, cx + x, y, cz + z, height))
                        }
                    }
                }
            }

            override fun generateSurface(
                worldInfo: WorldInfo,
                random: Random,
                chunkX: Int,
                chunkZ: Int,
                chunkData: ChunkData
            ) {
                val cx = chunkX * 16
                val cz = chunkZ * 16
                for (x in 0..15) {
                    for (z in 0..15) {
                        val height = getHeight(worldInfo, cx + x, cz + z)
                        for (y in height - surfaceHeight until height) {
                            chunkData.setBlock(x, y, z, surfaceGenerator(worldInfo, random, cx + x, y, cz + z, height))
                        }
                    }
                }
            }

            override fun generateBedrock(
                worldInfo: WorldInfo,
                random: Random,
                chunkX: Int,
                chunkZ: Int,
                chunkData: ChunkData
            ) {
                if (!generateBedrock) return
                val y = getMinHeight(worldInfo)
                for (x in 0..15) {
                    for (z in 0..15) {
                        chunkData.setBlock(x, y, z, Material.BEDROCK)
                    }
                }
            }

            private fun getHeight(worldInfo: WorldInfo, x: Int, z: Int): Int {
                if (!::baseNoise.isInitialized) {
                    baseNoise = SimplexOctaveGenerator(worldInfo.seed, octaves)
                    baseNoise.setScale(scale)
                }
                var height = baseNoise.noise(x.toDouble(), z.toDouble(), frequency, amplitude, true)
                height = height.pow(flattenFactor)
                height = (height + 1) / 2
                return (height * (averageHeight - minHeight)).toInt()
            }
        }
    }
}

fun PerlinBuilder.generateNoiseBlock(
    block: (WorldInfo, Random, Int, Int, Int, Int) -> Material
) {
    noiseGenerator = block
}

fun PerlinBuilder.generateSurfaceBlock(
    block: (WorldInfo, Random, Int, Int, Int, Int) -> Material
) {
    surfaceGenerator = block
}

object Perlin : GeneratorBuilderProvider<PerlinBuilder> {
    override fun provide() = PerlinBuilder()
}