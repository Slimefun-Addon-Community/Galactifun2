package io.github.addoncommunity.galactifun.scripting.dsl.gen

import io.github.addoncommunity.galactifun.util.worldgen.DoubleChunkGrid
import org.bukkit.util.noise.OctaveGenerator
import org.bukkit.util.noise.SimplexOctaveGenerator

class NoiseMap(noises: Map<String, AbstractPerlin.PerlinConfig>) {

    private val noises = noises.mapValues { Noise(it.value) }

    @Volatile
    private var initted = false

    internal fun init(seed: Long) {
        if (initted) return
        synchronized(this) {
            if (initted) return
            noises.values.forEach { it.init(seed) }
            initted = true
        }
    }

    class Noise internal constructor(config: AbstractPerlin.PerlinConfig) {

        private val octaves = config.octaves
        private val scale = config.scale
        private val amplitude = config.amplitude
        private val frequency = config.frequency
        private val smoothen = config.smoothen

        @Volatile
        private lateinit var noise: OctaveGenerator

        private val grid = DoubleChunkGrid()

        internal fun init(seed: Long) {
            noise = SimplexOctaveGenerator(seed, octaves)
            noise.setScale(scale)
        }

        operator fun invoke(x: Int, z: Int): Double {
            return grid.getOrSet(x, z) {
                var noise = noise.noise(x.toDouble(), z.toDouble(), frequency, amplitude, true)
                if (smoothen) {
                    noise *= noise
                }
                noise
            }
        }
    }
}