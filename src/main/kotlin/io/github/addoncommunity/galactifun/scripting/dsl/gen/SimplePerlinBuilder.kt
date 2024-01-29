package io.github.addoncommunity.galactifun.scripting.dsl.gen

import io.github.addoncommunity.galactifun.api.objects.planet.gen.WorldGenerator
import io.github.addoncommunity.galactifun.scripting.PlanetDsl
import io.github.addoncommunity.galactifun.scripting.RequiredProperty
import org.bukkit.Material
import org.bukkit.generator.WorldInfo
import org.bukkit.util.noise.OctaveGenerator
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.util.*
import kotlin.math.roundToInt

@PlanetDsl
class SimplePerlinBuilder : AbstractPerlin() {

    var noiseConfig: PerlinConfig by RequiredProperty()

    var blocks: SimpleBlocksBuilder by RequiredProperty()

    override fun build(): WorldGenerator {
        val octaves = noiseConfig.octaves
        val scale = noiseConfig.scale
        val frequency = noiseConfig.frequency
        val amplitude = noiseConfig.amplitude
        val smoothen = noiseConfig.smoothen
        val averageHeight = averageHeight
        val maxDeviation = maxDeviation

        val top = blocks.top
        val bottom = blocks.bottom
        val middle = blocks.middle

        val minHeight = minY + if (generateBedrock) 1 else 0
        val surfaceHeight = top.size

        return object : WorldGenerator() {

            override val biomeProvider = this@SimplePerlinBuilder.biomeProvider

            @Volatile
            private lateinit var baseNoise: OctaveGenerator

            override fun generateNoise(
                worldInfo: WorldInfo,
                random: Random,
                chunkX: Int,
                chunkZ: Int,
                chunkData: ChunkData
            ) {
                val cx = chunkX * 16
                val cz = chunkZ * 16
                val info = GenInfo(worldInfo, random, chunkX, chunkZ)
                val min = getMinHeight(worldInfo) + bottom.size
                for (x in 0..15) {
                    for (z in 0..15) {
                        val realX = cx + x
                        val realZ = cz + z
                        val height = getHeight(worldInfo, realX, realZ)
                        info.x = realX
                        info.z = realZ
                        info.height = height
                        for (y in min until height - surfaceHeight) {
                            info.y = y
                            chunkData.setBlock(x, y, z, middle(info))
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
                val info = GenInfo(worldInfo, random, chunkX, chunkZ)
                for (x in 0..15) {
                    for (z in 0..15) {
                        val realX = cx + x
                        val realZ = cz + z
                        val height = getHeight(worldInfo, realX, realZ)
                        info.x = realX
                        info.z = realZ
                        info.height = height
                        info.y = height
                        for (topBlock in top) {
                            info.y--
                            chunkData.setBlock(x, info.y, z, topBlock(info))
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
                var y = getMinHeight(worldInfo)
                if (generateBedrock) {
                    for (x in 0..15) {
                        for (z in 0..15) {
                            chunkData.setBlock(x, y, z, Material.BEDROCK)
                        }
                    }
                    y++
                }
                if (bottom.isEmpty()) return
                val cx = chunkX * 16
                val cz = chunkZ * 16
                val info = GenInfo(worldInfo, random, chunkX, chunkZ)
                for (x in 0..15) {
                    for (z in 0..15) {
                        val realX = cx + x
                        val realZ = cz + z
                        val height = getHeight(worldInfo, realX, realZ)
                        info.x = realX
                        info.z = realZ
                        info.height = height
                        info.y = y
                        for (bottomBlock in bottom) {
                            chunkData.setBlock(x, info.y, z, bottomBlock(info))
                            info.y++
                        }
                    }
                }
            }

            private fun getHeight(worldInfo: WorldInfo, x: Int, z: Int): Int {
                if (!::baseNoise.isInitialized) {
                    baseNoise = SimplexOctaveGenerator(worldInfo.seed, octaves)
                    baseNoise.setScale(scale)
                }

                var height = baseNoise.noise(x.toDouble(), z.toDouble(), frequency, amplitude, true)
                if (smoothen) {
                    height *= height
                }
                return (height * maxDeviation + averageHeight).roundToInt()
            }

            private fun getMinHeight(worldInfo: WorldInfo): Int = minHeight.coerceAtLeast(worldInfo.minHeight)
        }
    }

    data class GenInfo(
        val world: WorldInfo,
        val random: Random,
        val chunkX: Int,
        val chunkZ: Int,
    ) {
        var x: Int = 0
        var y: Int = 0
        var z: Int = 0
        var height: Int = 0
    }
}

inline fun SimplePerlinBuilder.configNoise(block: AbstractPerlin.PerlinConfig.() -> Unit) {
    noiseConfig = AbstractPerlin.PerlinConfig().apply(block)
}

inline fun SimplePerlinBuilder.blocks(block: SimpleBlocksBuilder.() -> Unit) {
    blocks = SimpleBlocksBuilder().apply(block)
}

object SimplePerlin : GeneratorBuilderProvider<SimplePerlinBuilder> {
    override fun provide(): SimplePerlinBuilder = SimplePerlinBuilder()
}