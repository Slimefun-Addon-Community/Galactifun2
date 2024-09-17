package io.github.addoncommunity.galactifun.util.menu

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap
import it.unimi.dsi.fastutil.chars.CharOpenHashSet
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuBuilder {

    private val menu = mutableListOf<String>()
    private val items = Char2ObjectOpenHashMap<MenuItem>()
    private val inputs = CharOpenHashSet()
    private val outputs = CharOpenHashSet()

    private var emptySlotsClickable = true

    operator fun String.unaryPlus() {
        require(length <= 9) { "Menu row must be 9 characters or less" }
        check(menu.size <= 6) { "Menu must be 6 rows or less" }
        menu.add(this)
    }

    infix fun Char.means(item: ItemStack): MenuItem {
        return MenuItem(item).also { items[this] = it }
    }

    fun background(c: Char) {
        items[c] = MenuItem(ChestMenuUtils.getBackground())
    }

    infix fun MenuItem.onClick(block: (Player, ClickAction) -> Boolean): MenuItem {
        onClick = block
        return this
    }

    fun input(c: Char) {
        inputs.add(c)
    }

    fun output(c: Char) {
        outputs.add(c)
    }

    fun apply(item: SlimefunItem) {
        object : BlockMenuPreset(item.id, item.itemName) {

            private val inputSlots = IntOpenHashSet()
            private val outputSlots = IntOpenHashSet()

            override fun init() {
                var slot = 0
                for (row in menu) {
                    for (char in row) {
                        val menuItem = items[char]
                        if (menuItem != null) {
                            addItem(slot, menuItem.item)
                            if (menuItem.onClick != null) {
                                addMenuClickHandler(slot) { p, _, _, action ->
                                    menuItem.onClick?.invoke(p, action) ?: false
                                }
                            }
                        }
                        if (inputs.contains(char)) {
                            inputSlots.add(slot)
                        }
                        if (outputs.contains(char)) {
                            outputSlots.add(slot)
                        }
                        slot++
                    }
                }
                isEmptySlotsClickable = emptySlotsClickable
            }

            override fun canOpen(b: Block, p: Player): Boolean {
                if (p.hasPermission("slimefun.inventory.bypass")) return true
                return item.canUse(p, false)
                        && Slimefun.getProtectionManager().hasPermission(p, b, Interaction.INTERACT_BLOCK)
            }

            override fun getSlotsAccessedByItemTransport(flow: ItemTransportFlow): IntArray {
                return when (flow) {
                    ItemTransportFlow.INSERT -> inputSlots.toIntArray()
                    ItemTransportFlow.WITHDRAW -> outputSlots.toIntArray()
                }
            }
        }
    }
}

inline fun buildMenu(block: MenuBuilder.() -> Unit): MenuBuilder = MenuBuilder().apply(block)