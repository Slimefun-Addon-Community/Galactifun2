package io.github.addoncommunity.galactifun.util.menu

import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

open class ItemContext(
    val menu: BlockMenu,
    val block: Block,
    val thisItem: ItemStack,
    private val slotMap: Map<Char, List<Int>>
) {
    fun getSlot(c: Char): Int = getSlots(c).single()

    fun getSlots(c: Char): List<Int> {
        return slotMap[c] ?: emptyList()
    }
}

class ClickContext(
    menu: BlockMenu,
    block: Block,
    thisItem: ItemStack,
    slotMap: Map<Char, List<Int>>
) : ItemContext(menu, block, thisItem, slotMap) {
    var allowTaking = false
}