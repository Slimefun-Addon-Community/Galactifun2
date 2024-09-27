package io.github.addoncommunity.galactifun.util.menu

import io.github.addoncommunity.galactifun.util.set
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap
import it.unimi.dsi.fastutil.chars.CharOpenHashSet
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu
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
        slots = null
    }

    infix fun Char.means(item: ItemStack) {
        this means MenuItem(item)
    }

    fun background(c: Char) {
        c means MenuItem(ChestMenuUtils.getBackground())
    }

    fun inputBorder(c: Char) {
        c means MenuItem(ChestMenuUtils.getInputSlotTexture())
    }

    fun outputBorder(c: Char) {
        c means MenuItem(ChestMenuUtils.getOutputSlotTexture())
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

    private var slots: MutableMap<Char, MutableList<Int>>? = null

    private val slotMap: Map<Char, List<Int>>
        get() {
            if (slots == null) {
                var slot = 0
                slots = mutableMapOf()
                for (row in menu) {
                    for (char in row) {
                        slots!!.getOrPut(char, ::mutableListOf).add(slot)
                        slot++
                    }
                }
            }
            return slots!!
        }

    fun apply(sfi: SlimefunItem) {
        // They have to be up here because init() is called in the superclass constructor smh
        val inputSlots = IntOpenHashSet()
        val outputSlots = IntOpenHashSet()
        val handlers = mutableListOf<Pair<Int, MenuItemClickHandler>>()
        val inits = mutableListOf<Pair<Int, ItemContext.() -> Unit>>()

        object : BlockMenuPreset(sfi.id, sfi.itemName) {
            override fun init() {
                for ((char, item) in items) {
                    val slots = slotMap[char] ?: continue
                    for (slot in slots) {
                        addItem(slot, item.item)
                        if (item.onClick != null) {
                            handlers.add(slot to item.onClick)
                        }
                        if (item.init != null) {
                            inits.add(slot to item.init)
                        }
                    }
                    if (inputs.contains(char)) {
                        inputSlots.addAll(slots)
                    }
                    if (outputs.contains(char)) {
                        outputSlots.addAll(slots)
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
                return sfi.canUse(p, true)
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

    fun newMenu(name: String, block: Block): ChestMenu {
        val menu = ChestMenu(name)
        for ((char, item) in items) {
            val slots = slotMap[char] ?: continue
            for (slot in slots) {
                menu.addItem(slot, item.item)
                if (item.onClick != null) {
                    menu.addMenuClickHandler(slot) { p, _, stack, action ->
                        val context = ClickContext(menu, block, stack, slotMap)
                        item.onClick.invoke(context, p, action)
                        context.allowTaking
                    }
                }
                if (item.init != null) {
                    val context = ItemContext(menu, block, item.item, slotMap)
                    item.init.invoke(context)
                }
            }
        }
        menu.isEmptySlotsClickable = emptySlotsClickable
        return menu
    }
}

inline fun buildMenu(
    builder: MenuBuilder = MenuBuilder(),
    block: MenuBuilder.() -> Unit
): MenuBuilder = builder.apply(block)