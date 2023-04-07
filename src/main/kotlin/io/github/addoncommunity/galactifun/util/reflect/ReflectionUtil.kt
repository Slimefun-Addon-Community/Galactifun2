package io.github.addoncommunity.galactifun.util.reflect

import kotlin.reflect.KCallable
import kotlin.reflect.KClass

fun KClass<*>.getFunction(name: String): KCallable<*> {
    return this.members.first { it.name == name }
}