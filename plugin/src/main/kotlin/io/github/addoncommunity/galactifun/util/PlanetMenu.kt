@file:Suppress("DEPRECATION")

package io.github.addoncommunity.galactifun.util

import io.github.addoncommunity.galactifun.api.objects.CelestialObject
import io.github.addoncommunity.galactifun.api.objects.MilkyWay
import io.github.addoncommunity.galactifun.api.objects.PlanetaryObject
import io.github.addoncommunity.galactifun.api.objects.Star
import io.github.addoncommunity.galactifun.impl.managers.PlanetManager
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils
import kotlinx.datetime.Clock
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.collections.ArrayDeque

open class PlanetMenu {

    private val history = mutableMapOf<UUID, ArrayDeque<CelestialObject>>()

    private val exitHandler = MenuClickHandler { player, _, _, _ ->
        onExit(player)
        false
    }

    fun open(p: Player) {
        openGalaxy(p)
    }

    private fun openGalaxy(p: Player) {
        val menu = ChestMenu("Milky Way Galaxy")
        menu.isEmptySlotsClickable = false
        menu[0] = ChestMenuUtils.getBackButton(p)
        menu[0] = exitHandler

        history.remove(p.uniqueId)

        val playerWorld = PlanetManager.getByWorld(p.world) ?: return
        var i = 1
        for (star in MilkyWay.stars) {
            addCelestialObject(i++, p, menu, playerWorld, star)
        }

        menu.open(p)
    }

    private fun open(p: Player, obj: CelestialObject) {
        val menu = ChestMenu(obj.name)
        menu.isEmptySlotsClickable = false
        menu[0] = ChestMenuUtils.getBackButton(p)
        menu[0] = MenuClickHandler { _, _, _, _ ->
            if (history.getOrPut(p.uniqueId, ::ArrayDeque).isNotEmpty()) {
                open(p, history.getValue(p.uniqueId).removeLast())
            } else {
                openGalaxy(p)
            }
            false
        }

        val playerWorld = PlanetManager.getByWorld(p.world) ?: return
        var i = 1
        for (child in obj.orbiters) {
            addCelestialObject(i++, p, menu, playerWorld, child)
        }

        menu.open(p)
    }

    private fun addCelestialObject(
        i: Int,
        p: Player,
        menu: ChestMenu,
        from: PlanetaryObject,
        to: CelestialObject
    ) {
        val now = Clock.System.now()
        val info = when (to) {
            is Star -> {
                val dist = from.distanceTo(to, now)
                val lore = mutableListOf<Component>()
                lore += NamedTextColor.GRAY + "Distance: %,.2f light years".format(dist.lightYears)
                lore += NamedTextColor.GRAY + "Planets: ${to.orbiters.size}"
                lore
            }

            is PlanetaryObject -> {
                val dist = from.distanceTo(to, now)
                val lore = mutableListOf<Component>()
                lore += NamedTextColor.GRAY + "Distance: %.2s".format(dist)
                lore += NamedTextColor.GRAY + "Moons: ${to.orbiters.size}"
                val dV = from.getDeltaVForTransferTo(to, now)
                lore += NamedTextColor.GRAY + "Delta-V for travel: %.2s".format(dV)
                lore
            }
        }
        val item = to.item.clone()
        item.lore(info)
        menu[i] = modifyItem(p, to, item)
        menu[i] = MenuClickHandler { _, _, _, action ->
            if (onClick(p, to, action) && to.orbiters.isNotEmpty()) {
                history.getOrPut(p.uniqueId, ::ArrayDeque).addLast(from)
                open(p, to)
            }
            false
        }
    }

    /**
     * Called when the menu is closed
     *
     * @param p The player who closed the menu
     */
    open fun onExit(p: Player) {
        p.closeInventory()
    }

    /**
     * Called when a player clicks on a celestial object
     *
     * @param p The player who clicked
     * @param obj The celestial object that was clicked
     * @param action The action that was performed
     * @return `true` if the menu should open the planet's children
     */
    open fun onClick(p: Player, obj: CelestialObject, action: ClickAction): Boolean {
        return true
    }

    /**
     * Modifies the item that represents a celestial object
     *
     * @param p The player who is viewing the menu
     * @param obj The celestial object that is being displayed
     * @param item The item that represents the celestial object
     */
    open fun modifyItem(p: Player, obj: CelestialObject, item: ItemStack): ItemStack {
        return item
    }
}