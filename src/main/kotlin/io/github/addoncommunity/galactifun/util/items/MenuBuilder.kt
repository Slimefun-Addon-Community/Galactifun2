package io.github.addoncommunity.galactifun.util.items

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntList
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack


class MenuBuilder {

    private val inputBorder = IntArrayList()
    private val outputBorder = IntArrayList()
    private val inputs = IntArrayList()
    private val outputs = IntArrayList()

    private val otherItems = mutableListOf<MenuItem>()

    var inputBorderItem = CustomItemStack(ChestMenuUtils.getInputSlotTexture(), "&9Input")
    var outputBorderItem = CustomItemStack(ChestMenuUtils.getOutputSlotTexture(), "&6Output")

    var numRows = 6
        set(value) {
            field = value.coerceIn(1, 6)
        }

    fun item(x: Int, y: Int, item: ItemStack, onClick: MenuClickHandler = ChestMenuUtils.getEmptyClickHandler()) {
        otherItems.add(MenuItem(coordsToSlot(x, y), item, onClick))
    }

    fun inputBorder(vararg points: Int) = inputBorder.addAll(points)

    fun outputBorder(vararg points: Int) = outputBorder.addAll(points)

    fun input(vararg points: Int): MaybeBorder {
        inputs.addAll(points)
        return MaybeBorder(points, inputBorder)
    }

    fun output(vararg points: Int): MaybeBorder {
        outputs.addAll(points)
        return MaybeBorder(points, outputBorder)
    }

    inner class MaybeBorder internal constructor(private val points: IntArray, private val borderList: IntList) {
        fun addBorder() {
            var i = 0
            while (i < points.size) {
                val cx = points[i++]
                val cy = points[i++]
                for (x in -1..1) {
                    for (y in -1..1) {
                        val result = coordsToSlot(cx + x, cy + y)
                        if (result !in inputs && result !in outputs && result in 0 until numRows * 9) {
                            borderList.add(result)
                        }
                    }
                }
            }
        }
    }

    fun applyOn(sfItem: SlimefunItem) {
        val lastIndex = numRows * 9
        val ins = inputs.toIntArray()
        val outs = outputs.toIntArray()
        val inBorder = inputBorder.toIntArray()
        val outBorder = outputBorder.toIntArray()

        val bg = IntOpenHashSet(IntArray(lastIndex) { it })
        ins.forEach(bg::remove)
        outs.forEach(bg::remove)
        inBorder.forEach(bg::remove)
        outBorder.forEach(bg::remove)
        otherItems.forEach { bg.remove(it.slot) }

        object : BlockMenuPreset(sfItem.id, sfItem.itemName) {
            override fun init() {
                for (index in inBorder) {
                    this.addItem(index, inputBorderItem, ChestMenuUtils.getEmptyClickHandler())
                }
                for (index in outBorder) {
                    this.addItem(index, outputBorderItem, ChestMenuUtils.getEmptyClickHandler())
                }
                for (item in otherItems) {
                    this.addItem(item.slot, item.item, item.handler)
                }
                for (index in bg.toIntArray()) {
                    this.addItem(index, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler())
                }
                this.isEmptySlotsClickable = true
            }

            override fun canOpen(b: Block, p: Player) =
                p.hasPermission("slimefun.inventory.bypass")
                        || (sfItem.canUse(p, false)
                        && Slimefun.getProtectionManager().hasPermission(p, b, Interaction.INTERACT_BLOCK)
                        )

            override fun getSlotsAccessedByItemTransport(flow: ItemTransportFlow): IntArray {
                return when (flow) {
                    ItemTransportFlow.INSERT -> ins
                    ItemTransportFlow.WITHDRAW -> outs
                }
            }
        }
    }
}

private fun coordsToSlot(x: Int, y: Int): Int {
    return x + y * 9
}

private fun IntList.addAll(array: IntArray) {
    array.forEach(this::add)
}

private data class MenuItem(val slot: Int, val item: ItemStack, val handler: MenuClickHandler)

inline fun buildMenu(block: MenuBuilder.() -> Unit): MenuBuilder {
    return MenuBuilder().apply(block)
}