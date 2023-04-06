package io.github.addoncommunity.galactifun.api.objects.properties.atmosphere

import org.bukkit.World
import java.util.EnumMap

class AtmosphereBuilder internal constructor() {

    var weatherEnabled = false
    var storming = false
    var thundering = false
    var pressure = 1.0
    var environment = World.Environment.NORMAL

    internal val composition = EnumMap<Gas, Double>(Gas::class.java)

    fun addGas(gas: Gas, percent: Double) {
        composition[gas] = percent
    }


}