package io.github.addoncommunity.galactifun.util

import io.github.addoncommunity.galactifun.pluginInstance
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import java.util.*

fun String.toKey(): NamespacedKey = NamespacedKey(pluginInstance, this)

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

operator fun RegionAccessor.set(x: Int, y: Int, z: Int, material: Material) = setType(x, y, z, material)

inline fun <reified E : Enum<E>> enumSetOf(vararg elements: E): EnumSet<E> {
    val set = EnumSet.noneOf(E::class.java)
    set.addAll(elements)
    return set
}

inline fun <reified E : Enum<E>> enumSetOf(): EnumSet<E> = EnumSet.noneOf(E::class.java)

inline fun BlockTicker(sync: Boolean, crossinline tick: (Block) -> Unit) = object : BlockTicker() {
    override fun isSynchronized() = sync
    override fun tick(b: Block, item: SlimefunItem, data: Config) = tick(b)
}