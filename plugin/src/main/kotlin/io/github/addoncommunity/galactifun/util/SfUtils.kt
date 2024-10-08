@file:Suppress("DEPRECATION")

package io.github.addoncommunity.galactifun.util

import io.github.thebusybiscuit.slimefun4.api.items.ItemSetting
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.RandomizedSet
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler
import me.mrCookieSlime.Slimefun.api.BlockStorage
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KProperty

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

operator fun <T> ItemSetting<T>.getValue(thisRef: SlimefunItem, property: KProperty<*>): T = value

inline fun <T> buildRandomizedSet(builder: RandomizedSet<T>.() -> Unit): RandomizedSet<T> =
    RandomizedSet<T>().apply(builder)

inline fun <reified S : SlimefunItem, T> Iterable<Block>.processSlimefunBlocks(
    processor: S.(Block) -> T
): List<T> {
    val result = mutableListOf<T>()
    for (b in this) {
        val item = BlockStorage.check(b)
        if (item is S) {
            result.add(item.processor(b))
        }
    }
    return result
}

@JvmName("processSlimefunBlocksPosition")
inline fun <reified S : SlimefunItem, T> Iterable<BlockPosition>.processSlimefunBlocks(
    processor: S.(Block) -> T
): List<T> = map(BlockPosition::getBlock).processSlimefunBlocks(processor)

operator fun ChestMenu.set(slot: Int, item: ItemStack) {
    addItem(slot, item)
}

operator fun ChestMenu.set(slot: Int, handler: MenuClickHandler) {
    addMenuClickHandler(slot, handler)
}
