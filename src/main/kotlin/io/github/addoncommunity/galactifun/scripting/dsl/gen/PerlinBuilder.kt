package io.github.addoncommunity.galactifun.scripting.dsl.gen

import io.github.addoncommunity.galactifun.api.objects.planet.gen.WorldGenerator
import io.github.addoncommunity.galactifun.scripting.RequiredProperty
import org.bukkit.Material
import org.bukkit.generator.WorldInfo
import java.util.*
import kotlin.math.roundToInt

class PerlinBuilder : AbstractPerlin() {

    var noiseGenerator: GenInfo.() -> Material by RequiredProperty()
    var noiseCombiner: NoiseInfo.() -> Double by RequiredProperty()

    val noises = mutableMapOf<String, PerlinConfig>()

    override fun build(): WorldGenerator {
        val averageHeight = averageHeight
        val maxDeviation = maxDeviation
        val minHeight = minHeight + if (generateBedrock) 1 else 0

        val noises = NoiseMap(noises)

        return object : WorldGenerator() {

            override val biomeProvider = this@PerlinBuilder.biomeProvider

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
                val info = GenInfo(worldInfo, random, chunkX, chunkZ)
                for (x in 0..15) {
                    for (z in 0..15) {
                        val realX = cx + x
                        val realZ = cz + z
                        val height = getHeight(worldInfo, random, realX, realZ)
                        info.x = realX
                        info.z = realZ
                        info.height = height
                        for (y in min until height - surfaceHeight) {
                            info.y = y
                            chunkData.setBlock(x, y, z, noiseGenerator(info))
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
                        val height = getHeight(worldInfo, random, realX, realZ)
                        info.x = realX
                        info.z = realZ
                        info.height = height
                        for (y in height - surfaceHeight until height) {
                            info.y = y
                            chunkData.setBlock(x, y, z, noiseGenerator(info))
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

            private fun getHeight(worldInfo: WorldInfo, random: Random, x: Int, z: Int): Int {
                noises.init(worldInfo.seed)
                val height = noiseCombiner(NoiseInfo(worldInfo, noises, random, x, z))
                return (height * maxDeviation + averageHeight).roundToInt()
            }
        }
    }

    data class GenInfo(
        val world: WorldInfo,
        val random: Random,
        val chunkX: Int,
        val chunkZ: Int
    ) {
        var x: Int = 0
        var y: Int = 0
        var z: Int = 0
        var height: Int = 0
    }

    data class NoiseInfo(val world: WorldInfo, val noise: NoiseMap, val random: Random, val x: Int, val z: Int)
}

fun PerlinBuilder.generateBlock(block: PerlinBuilder.GenInfo.() -> Material) {
    noiseGenerator = block
}

fun PerlinBuilder.combineNoise(noise: PerlinBuilder.NoiseInfo.() -> Double) {
    noiseCombiner = noise
}

fun PerlinBuilder.noiseConfig(name: String, config: AbstractPerlin.PerlinConfig.() -> Unit) {
    noises[name] = AbstractPerlin.PerlinConfig().apply(config)
}

object MultiNoise : GeneratorBuilderProvider<PerlinBuilder> {
    override fun provide() = PerlinBuilder()
}