package io.github.addoncommunity.galactifun.scripting.dsl.gen

import org.bukkit.generator.WorldInfo
import java.util.*

interface NoiseCombiner {
    var noiseCombiner: NoiseInfo.() -> Double

    data class NoiseInfo(val world: WorldInfo, val noise: NoiseMap, val random: Random, val x: Int, val z: Int)
}

fun NoiseCombiner.combineNoise(noise: NoiseCombiner.NoiseInfo.() -> Double) {
    noiseCombiner = noise
}