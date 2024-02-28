package io.github.addoncommunity.galactifun.util

import io.github.addoncommunity.galactifun.pluginInstance
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.RandomizedSet
import me.mrCookieSlime.Slimefun.api.BlockStorage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.metadata.FixedMetadataValue
import java.util.*
import java.util.concurrent.CompletableFuture

fun String.key(): NamespacedKey = NamespacedKey(pluginInstance, this)

fun Location.withWorld(world: World): Location = Location(world, x, y, z, yaw, pitch)

inline fun <reified T : Entity> World.getNearbyEntitiesByType(
    location: Location,
    radius: Double,
    crossinline predicate: (T) -> Boolean = { true }
): List<T> = buildList {
    for (entity in getNearbyEntities(location, radius, radius, radius)) {
        if (entity is T && predicate(entity)) {
            add(entity)
        }
    }
}

inline fun <reified T : Entity> World.spawn(location: Location): T = spawn(location, T::class.java)

operator fun RegionAccessor.get(x: Int, y: Int, z: Int): Material = getType(x, y, z)
operator fun RegionAccessor.get(location: Location): Material = getType(location)

operator fun RegionAccessor.set(x: Int, y: Int, z: Int, material: Material) = setType(x, y, z, material)
operator fun RegionAccessor.set(location: Location, material: Material) = setType(location, material)

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

inline fun <T> buildRandomizedSet(builder: RandomizedSet<T>.() -> Unit): RandomizedSet<T> =
    RandomizedSet<T>().apply(builder)

/**
 * Teleports while telling Galactifun that the teleport should not be blocked
 */
fun Entity.galactifunTeleport(
    dest: Location,
    reason: PlayerTeleportEvent.TeleportCause = PlayerTeleportEvent.TeleportCause.PLUGIN
): CompletableFuture<Boolean> {
    setMetadata("galactifun.teleporting", FixedMetadataValue(pluginInstance, Unit))
    return teleportAsync(dest, reason).thenApply {
        removeMetadata("galactifun.teleporting", pluginInstance)
        it
    }
}

inline fun <reified I : SlimefunItem> Location.checkBlock(): Block? {
    return if (BlockStorage.check(this) is I) block else null
}

inline fun <reified I : SlimefunItem> Block.checkBlock(): Block? = location.checkBlock<I>()

inline fun <K, V> Map<K, V>.mergeMaps(other: Map<K, V>, merge: (V, V) -> V): Map<K, V> {
    val result = this.toMutableMap()
    for ((k, v) in other) {
        val thisVal = this[k]
        result[k] = if (thisVal != null) merge(thisVal, v) else v
    }
    return result
}

operator fun TextColor.plus(s: String): TextComponent = Component.text().color(this).content(s).build()
