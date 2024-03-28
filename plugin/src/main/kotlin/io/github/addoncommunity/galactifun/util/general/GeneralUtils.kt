package io.github.addoncommunity.galactifun.util.general

import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

inline fun <K, V> Map<K, V>.mergeMaps(other: Map<K, V>, merge: (V, V) -> V): Map<K, V> {
    val result = this.toMutableMap()
    for ((k, v) in other) {
        val thisVal = this[k]
        result[k] = if (thisVal != null) merge(thisVal, v) else v
    }
    return result
}

inline fun <reified E : Enum<E>> enumSetOf(vararg elements: E): EnumSet<E> {
    val set = EnumSet.noneOf(E::class.java)
    set.addAll(elements)
    return set
}

inline fun <reified K : Enum<K>, V> enumMapOf(vararg pairs: Pair<K, V>): EnumMap<K, V> {
    val map: EnumMap<K, V> = EnumMap(K::class.java)
    map.putAll(pairs)
    return map
}

inline fun <reified E : Enum<E>> enumSetOf(): EnumSet<E> = EnumSet.noneOf(E::class.java)

inline fun <reified K : Enum<K>, V> enumMapOf(): EnumMap<K, V> = EnumMap(K::class.java)

fun Logger.log(message: String) {
    log(Level.INFO, message)
}