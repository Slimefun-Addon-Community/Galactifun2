package io.github.addoncommunity.galactifun.scripting

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class RequiredProperty<T>(private var value: T? = null) : ReadWriteProperty<Any, T> {

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return value ?: error("${property.name} must be set")
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        this.value = value
    }
}