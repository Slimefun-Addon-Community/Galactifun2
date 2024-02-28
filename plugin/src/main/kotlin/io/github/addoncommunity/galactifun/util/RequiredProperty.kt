package io.github.addoncommunity.galactifun.util

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class RequiredProperty<T>(
    private var value: T? = null,
    private val getter: (T) -> T = { it },
    private val setter: (T) -> T = { it }
) : ReadWriteProperty<Any, T> {

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return getter(value ?: error("${property.name} must be set"))
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        this.value = setter(value)
    }
}