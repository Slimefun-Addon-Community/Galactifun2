package io.github.addoncommunity.galactifun.util.general

import kotlin.reflect.KProperty

class LazyDouble(private val supplier: () -> Double)  {

    private var value = Double.NaN
    private var initialized = false

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Double {
        if (!initialized) {
            value = supplier()
            initialized = true
        }
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Double) {
        this.value = value
        initialized = true
    }
}