package io.github.addoncommunity.galactifun.util.menu

import io.github.addoncommunity.galactifun.util.bukkit.legacyDefaultColor
import io.github.addoncommunity.galactifun.util.bukkit.miniMessageToLegacy
import io.github.addoncommunity.galactifun.util.general.RequiredProperty
import io.github.addoncommunity.galactifun.util.items.MaterialType
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

typealias MenuItemClickHandler = ClickContext.(Player, ClickAction) -> Unit
typealias MenuItemInitHandler = ItemContext.() -> Unit

class MenuItem(
    val item: ItemStack,
    val onClick: MenuItemClickHandler? = { _, _ -> },
    val init: MenuItemInitHandler? = null
) {

    init {
        item.editMeta { it.setMaxStackSize(99) }
    }

    class Builder {
        var name: String by RequiredProperty()
        var material: MaterialType by RequiredProperty()
        val lore = mutableListOf<String>()

        var onClick: MenuItemClickHandler? = { _, _ -> }
        var init: MenuItemInitHandler? = null

        operator fun String.unaryPlus() {
            lore += this.miniMessageToLegacy()
        }

        fun onClick(handler: MenuItemClickHandler) {
            onClick = handler
        }

        fun init(builder: MenuItemInitHandler) {
            init = builder
        }

        fun build(): MenuItem {
            val item = CustomItemStack(
                material.convert(),
                name.miniMessageToLegacy().legacyDefaultColor('f'),
                *lore.map { it.legacyDefaultColor('7') }.toTypedArray()
            )
            return MenuItem(item, onClick, init)
        }
    }
}