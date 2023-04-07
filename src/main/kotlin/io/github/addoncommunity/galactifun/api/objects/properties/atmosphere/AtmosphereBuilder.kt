package io.github.addoncommunity.galactifun.api.objects.properties.atmosphere

import org.bukkit.World
import java.util.EnumMap

@AtmosphereDsl
class AtmosphereBuilder internal constructor() {

    var weatherEnabled = false
    var storming = false
    var thundering = false
    var pressure = 1.0
    var environment = World.Environment.NORMAL

    internal val composition = EnumMap<Gas, Double>(Gas::class.java)

    @AtmosphereDsl
    inner class CompositionBuilder internal constructor() {
        infix fun Double.percent(gas: Gas) {
            this@AtmosphereBuilder.composition[gas] = this
        }
    }

    fun composition(builder: CompositionBuilder.() -> Unit) {
        val compositionBuilder = CompositionBuilder()
        builder(compositionBuilder)
    }
}

@DslMarker
annotation class AtmosphereDsl