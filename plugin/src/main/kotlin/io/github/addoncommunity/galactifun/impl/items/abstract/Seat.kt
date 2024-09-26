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
import org.bukkit.entity.Arrow
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
            val entity = player.vehicle as? Arrow ?: return null
            return entity.getPdc(armorStandKey)
        }
    }

    @ItemHandler(BlockPlaceHandler::class)
    private fun onPlace(e: BlockPlaceEvent) {
        val b = e.block
        val entity = b.world.summon<Arrow>(b.location.toStandLocation())
        entity.isInvisible = true
        entity.isInvulnerable = true
        entity.setGravity(false)
        entity.setPdc(armorStandKey, b.location)
        val data = b.blockData
        if (data is Directional) {
            val facing = data.facing
            val location = entity.location
            location.yaw = atan2(facing.modZ.toDouble(), facing.modX.toDouble()).radians
                .degrees.toFloat() + 90
            entity.teleportAsync(location)
        }
    }

    @ItemHandler(SimpleBlockBreakHandler::class)
    private fun onBreak(b: Block) {
        val entity = b.world.nearbyEntitiesByType<Arrow>(b.location.toStandLocation(), 0.5) {
            it.persistentDataContainer.has(armorStandKey)
        }.firstOrNull() ?: return
        entity.remove()
    }

    @ItemHandler(BlockUseHandler::class)
    private fun onUse(e: PlayerRightClickEvent) {
        val block = e.clickedBlock.get()
        val entity = block.world.nearbyEntitiesByType<Arrow>(block.location.toStandLocation(), 0.5) {
            it.persistentDataContainer.has(armorStandKey)
        }.firstOrNull() ?: return
        entity.addPassenger(e.player)
        onSit(e.player, block)
    }

    protected open fun onSit(p: Player, b: Block) {}
}

private fun Location.toStandLocation() = add(0.5, 0.4, 0.5)