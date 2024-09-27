package io.github.addoncommunity.galactifun.impl.items.rocket

import io.github.addoncommunity.galactifun.api.betteritem.BetterSlimefunItem
import io.github.addoncommunity.galactifun.api.betteritem.ItemHandler
import io.github.addoncommunity.galactifun.api.rockets.RocketInfo
import io.github.addoncommunity.galactifun.impl.managers.RocketManager
import io.github.addoncommunity.galactifun.units.Angle.Companion.radians
import io.github.addoncommunity.galactifun.util.bukkit.*
import io.github.seggan.sf4k.serial.blockstorage.getBlockStorage
import io.github.seggan.sf4k.serial.blockstorage.setBlockStorage
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import me.mrCookieSlime.Slimefun.api.BlockStorage
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.data.Directional
import org.bukkit.entity.Arrow
import org.bukkit.entity.Pig
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.atan2

class Seat(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>
) : BetterSlimefunItem(itemGroup, item, recipeType, recipe) {

    @ItemHandler(BlockPlaceHandler::class)
    private fun onPlace(e: BlockPlaceEvent) {
        val b = e.block
        val entity = b.world.summon<Pig>(b.location.toStandLocation())
        entity.isInvisible = true
        entity.isInvulnerable = true
        entity.setAI(false)
        entity.setGravity(false)
        entity.setPdc(SEAT_KEY, b.location)
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
            it.persistentDataContainer.has(SEAT_KEY)
        }.firstOrNull() ?: return
        entity.remove()
    }

    @ItemHandler(BlockUseHandler::class)
    private fun onUse(e: PlayerRightClickEvent) {
        val block = e.clickedBlock.get()
        val entity = block.world.nearbyEntitiesByType<Arrow>(block.location.toStandLocation(), 0.5) {
            it.persistentDataContainer.has(SEAT_KEY)
        }.firstOrNull() ?: return
        entity.addPassenger(e.player)
    }

    companion object {

        private const val ROCKET_KEY = "rocket"
        private val SEAT_KEY = "seat".key()

        fun getSitting(player: Player): Location? {
            val entity = player.vehicle as? Arrow ?: return null
            val location = entity.getPdc<Location>(SEAT_KEY) ?: return null
            if (BlockStorage.check(location) !is Seat) return null
            return location
        }

        fun getRocket(l: Location): RocketInfo? {
            val commandComputer = l.getBlockStorage<BlockPosition>(ROCKET_KEY) ?: return null
            return RocketManager.getInfo(commandComputer)
        }

        fun setRocket(seat: Location, pos: BlockPosition) {
            seat.setBlockStorage<BlockPosition>(ROCKET_KEY, pos)
        }
    }
}

private fun Location.toStandLocation() = add(0.5, 0.4, 0.5)