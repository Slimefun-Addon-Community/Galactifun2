package io.github.addoncommunity.galactifun.util.menu

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuItem(val item: ItemStack) {
    var onClick: ((Player, ClickAction) -> Boolean)? = { _, _ -> false }
}