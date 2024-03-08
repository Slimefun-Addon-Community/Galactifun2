package io.github.addoncommunity.galactifun.util

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.RandomizedSet
import me.mrCookieSlime.Slimefun.api.BlockStorage
import org.bukkit.Location
import org.bukkit.block.Block

/**
 * Checks if the block at the given location is a Slimefun item of the given type.
 *
 * @param I the type of Slimefun item to check for
 *
 * @return the block at the given location if it is a Slimefun item of the given type, or `null` if it is not
 */
inline fun <reified I : SlimefunItem> Location.checkBlock(): Block? {
    return if (BlockStorage.check(this) is I) block else null
}

inline fun <reified I : SlimefunItem> Block.checkBlock(): Block? = location.checkBlock<I>()

inline fun <T> buildRandomizedSet(builder: RandomizedSet<T>.() -> Unit): RandomizedSet<T> =
    RandomizedSet<T>().apply(builder)