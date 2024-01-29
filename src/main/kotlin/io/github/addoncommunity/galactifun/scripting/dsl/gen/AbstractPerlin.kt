package io.github.addoncommunity.galactifun.scripting.dsl.gen

import io.github.addoncommunity.galactifun.scripting.PlanetDsl
import io.github.addoncommunity.galactifun.scripting.RequiredProperty

abstract class AbstractPerlin : GeneratorBuilder() {
    var generateBedrock = true

    var averageHeight: Int by RequiredProperty()
    var maxDeviation: Int by RequiredProperty()
    var minY = Int.MIN_VALUE
    var surfaceHeight = 10

    @PlanetDsl
    class PerlinConfig {
        var octaves: Int = 8
        var scale: Double by RequiredProperty()
        var amplitude: Double by RequiredProperty()
        var frequency: Double by RequiredProperty()

        var smoothen: Boolean = false
    }
}