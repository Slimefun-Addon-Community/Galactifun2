package io.github.addoncommunity.galactifun.scripting.dsl.gen

import io.github.addoncommunity.galactifun.scripting.PlanetDsl
import io.github.addoncommunity.galactifun.scripting.RequiredProperty

@PlanetDsl
abstract class AbstractPerlin : GeneratorBuilder() {

    var config: PerlinConfig by RequiredProperty()

    var generateBedrock = true

    var averageHeight: Int by RequiredProperty()
    var minHeight: Int by RequiredProperty()
    var surfaceHeight = 10

    class PerlinConfig {
        var octaves: Int = 8
        var scale: Double by RequiredProperty()
        var amplitude: Double by RequiredProperty()
        var frequency: Double by RequiredProperty()

        var flattenFactor: Double = 1.0
        var smoothen: Boolean = false
    }
}

inline fun AbstractPerlin.noiseConfig(block: AbstractPerlin.PerlinConfig.() -> Unit) {
    config = AbstractPerlin.PerlinConfig().apply(block)
}