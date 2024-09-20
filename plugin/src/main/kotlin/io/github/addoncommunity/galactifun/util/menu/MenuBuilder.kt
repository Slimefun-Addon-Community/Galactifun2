package io.github.addoncommunity.galactifun.util.menu

import io.github.addoncommunity.galactifun.util.set
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap
import it.unimi.dsi.fastutil.chars.CharOpenHashSet
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu
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

    infix fun Char.means(item: MenuItem) {
        items[this] = item
    }

    infix fun Char.means(item: ItemStack) {
        items[this] = MenuItem(item)
    }

    fun background(c: Char) {
        items[c] = MenuItem(ChestMenuUtils.getBackground())
    }

    fun inputBorder(c: Char) {
        items[c] = MenuItem(ChestMenuUtils.getInputSlotTexture())
    }

    fun outputBorder(c: Char) {
        items[c] = MenuItem(ChestMenuUtils.getOutputSlotTexture())
    }

    fun input(c: Char) {
        inputs.add(c)
    }

    fun output(c: Char) {
        outputs.add(c)
    }

    fun item(builder: MenuItem.Builder.() -> Unit): MenuItem {
        return MenuItem.Builder().apply(builder).build()
    }

    fun apply(item: SlimefunItem) {
        // They have to be up here because init() is called in the superclass constructor smh
        val inputSlots = IntOpenHashSet()
        val outputSlots = IntOpenHashSet()
        val slotMap = mutableMapOf<Char, MutableList<Int>>()
        val handlers = mutableListOf<Pair<Int, MenuItemClickHandler>>()
        val inits = mutableListOf<Pair<Int, ItemContext.() -> Unit>>()

        object : BlockMenuPreset(item.id, item.itemName) {
            override fun init() {
                var slot = 0
                for (row in menu) {
                    for (char in row) {
                        slotMap.getOrPut(char, ::mutableListOf).add(slot)
                        val menuItem = items[char]
                        if (menuItem != null) {
                            addItem(slot, menuItem.item)
                            if (menuItem.onClick != null) {
                                handlers.add(slot to menuItem.onClick)
                            }
                            if (menuItem.init != null) {
                                inits.add(slot to menuItem.init)
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

            override fun newInstance(menu: BlockMenu, b: Block) {
                for ((slot, handler) in handlers) {
                    menu[slot] = { p, _, item, action ->
                        val context = ClickContext(menu, b, item, slotMap)
                        context.handler(p, action)
                        context.allowTaking
                    }
                }
                for ((slot, init) in inits) {
                    val context = ItemContext(menu, b, menu.getItemInSlot(slot), slotMap)
                    context.init()
                }
            }

            override fun canOpen(b: Block, p: Player): Boolean {
                if (p.hasPermission("slimefun.inventory.bypass")) return true
                return item.canUse(p, true)
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