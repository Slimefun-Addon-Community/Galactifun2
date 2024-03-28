@file:Suppress("DEPRECATION")

package io.github.addoncommunity.galactifun.util

import io.github.addoncommunity.galactifun.api.objects.CelestialObject
import io.github.addoncommunity.galactifun.api.objects.MilkyWay
import io.github.addoncommunity.galactifun.api.objects.PlanetaryObject
import io.github.addoncommunity.galactifun.api.objects.Star
import io.github.addoncommunity.galactifun.impl.managers.PlanetManager
import io.github.addoncommunity.galactifun.util.PlanetMenu.PlanetClickHandler
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

class PlanetMenu(
    private val exitHandler: MenuClickHandler = MenuClickHandler { p, _, _, _ -> p.closeInventory(); false },
    private val clickHandler: PlanetClickHandler = PlanetClickHandler { _, _, _ -> true },
    private val modifier: (Player, CelestialObject, ItemStack) -> ItemStack = { _, _, item -> item }
) {

    private val history = mutableMapOf<UUID, ArrayDeque<CelestialObject>>()

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
        menu[i] = modifier(p, to, item)
        menu[i] = MenuClickHandler { _, _, _, action ->
            if (clickHandler.onClick(p, to, action) && to.orbiters.isNotEmpty()) {
                history.getOrPut(p.uniqueId, ::ArrayDeque).addLast(from)
                open(p, to)
            }
            false
        }
    }

    fun interface PlanetClickHandler {
        /**
         * @param p The player who clicked
         * @param obj The celestial object that was clicked
         * @param action The action that was performed
         * @return True if the menu should open the planet's children
         */
        fun onClick(p: Player, obj: CelestialObject, action: ClickAction): Boolean
    }
}