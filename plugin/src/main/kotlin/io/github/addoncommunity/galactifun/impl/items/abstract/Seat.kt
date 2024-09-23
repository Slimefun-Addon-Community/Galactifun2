package io.github.addoncommunity.galactifun.impl.items.abstract

import io.github.addoncommunity.galactifun.api.betteritem.BetterSlimefunItem
import io.github.addoncommunity.galactifun.api.betteritem.ItemHandler
import io.github.addoncommunity.galactifun.units.Angle.Companion.radians
import io.github.addoncommunity.galactifun.util.bukkit.*
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.data.Directional
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.atan2

open class Seat(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>
) : BetterSlimefunItem(itemGroup, item, recipeType, recipe) {

    companion object {
        private val armorStandKey = "seat".key()

        fun getSitting(player: Player): Location? {
            val armorStand = player.vehicle as? ArmorStand ?: return null
            return armorStand.getPdc(armorStandKey)
        }
    }

    @ItemHandler(BlockPlaceHandler::class)
    private fun onPlace(e: BlockPlaceEvent) {
        val b = e.block
        val armorStand = b.world.summon<ArmorStand>(b.location.toStandLocation())
        armorStand.isInvisible = true
        armorStand.isInvulnerable = true
        armorStand.isSmall = true
        armorStand.setGravity(false)
        armorStand.setAI(false)
        armorStand.isMarker = true
        armorStand.setPdc(armorStandKey, b.location)
        val data = b.blockData
        if (data is Directional) {
            val facing = data.facing
            val location = armorStand.location
            location.yaw = atan2(facing.modZ.toDouble(), facing.modX.toDouble()).radians
                .degrees.toFloat() + 90
            armorStand.teleportAsync(location)
        }
    }

    @ItemHandler(SimpleBlockBreakHandler::class)
    private fun onBreak(b: Block) {
        val armorStand = b.world.nearbyEntitiesByType<ArmorStand>(b.location.toStandLocation(), 0.5) {
            it.persistentDataContainer.has(armorStandKey)
        }.firstOrNull() ?: return
        armorStand.remove()
    }

    @ItemHandler(BlockUseHandler::class)
    private fun onUse(e: PlayerRightClickEvent) {
        val block = e.clickedBlock.get()
        val armorStand = block.world.nearbyEntitiesByType<ArmorStand>(block.location.toStandLocation(), 0.5) {
            it.persistentDataContainer.has(armorStandKey)
        }.firstOrNull() ?: return
        armorStand.addPassenger(e.player)
        onSit(e.player, block)
    }

    protected open fun onSit(p: Player, b: Block) {}
}

private fun Location.toStandLocation() = add(0.5, 0.4, 0.5)