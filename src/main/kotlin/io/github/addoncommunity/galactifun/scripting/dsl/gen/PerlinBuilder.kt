package io.github.addoncommunity.galactifun.scripting.dsl.gen

import io.github.addoncommunity.galactifun.api.objects.planet.gen.WorldGenerator
import io.github.addoncommunity.galactifun.scripting.PlanetDsl
import io.github.addoncommunity.galactifun.scripting.RequiredProperty
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.WorldInfo
import java.util.*
import kotlin.math.roundToInt

@PlanetDsl
class PerlinBuilder : AbstractPerlin(), NoiseCombiner {

    var noiseGenerator: GenInfo.() -> Material by RequiredProperty()
    override var noiseCombiner: NoiseCombiner.NoiseInfo.() -> Double by RequiredProperty()

    var biomeBuilder: BiomeBuilder? = null

    val noises = mutableMapOf<String, PerlinConfig>()

    override fun build(): WorldGenerator {
        val averageHeight = averageHeight
        val maxDeviation = maxDeviation
        val minHeight = minY + if (generateBedrock) 1 else 0

        val noises = NoiseMap(noises)

        return object : WorldGenerator() {

            override val biomeProvider = biomeBuilder?.build(noises) ?: this@PerlinBuilder.biomeProvider

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
                val info = GenInfo(worldInfo, random, chunkX, chunkZ, noises)
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
                val info = GenInfo(worldInfo, random, chunkX, chunkZ, noises)
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
                val height = noiseCombiner(NoiseCombiner.NoiseInfo(worldInfo, noises, random, x, z))
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
        val noise: NoiseMap,
    ) {
        var x: Int = 0
        var y: Int = 0
        var z: Int = 0
        var height: Int = 0
    }

    @PlanetDsl
    class BiomeBuilder {
        var biomeGenerator: BiomeInfo.() -> Biome by RequiredProperty()

        var biomes = mutableListOf<Biome>()

        fun build(noises: NoiseMap): BiomeProvider {
            return object : BiomeProvider() {

                override fun getBiome(worldInfo: WorldInfo, x: Int, y: Int, z: Int): Biome {
                    return biomeGenerator(BiomeInfo(worldInfo, x / 16, z / 16, noises, x, y, z))
                }

                override fun getBiomes(worldInfo: WorldInfo): MutableList<Biome> = biomes
            }
        }

        data class BiomeInfo(
            val world: WorldInfo,
            val chunkX: Int,
            val chunkZ: Int,
            val noise: NoiseMap,
            val x: Int,
            val y: Int,
            val z: Int,
        )
    }
}

fun PerlinBuilder.generateBlock(block: PerlinBuilder.GenInfo.() -> Material) {
    noiseGenerator = block
}

fun PerlinBuilder.configNoise(name: String, config: AbstractPerlin.PerlinConfig.() -> Unit) {
    noises[name] = AbstractPerlin.PerlinConfig().apply(config)
}

fun PerlinBuilder.biomes(config: PerlinBuilder.BiomeBuilder.() -> Unit) {
    biomeBuilder = PerlinBuilder.BiomeBuilder().apply(config)
}

object MultiNoise : GeneratorBuilderProvider<PerlinBuilder> {
    override fun provide() = PerlinBuilder()
}