package io.github.addoncommunity.galactifun.impl.items

import io.github.addoncommunity.galactifun.api.rockets.RocketInfo
import io.github.addoncommunity.galactifun.impl.managers.RocketManager
import io.github.addoncommunity.galactifun.util.checkBlock
import io.github.addoncommunity.galactifun.util.floodSearch
import io.github.addoncommunity.galactifun.util.items.TickingBlock
import io.github.addoncommunity.galactifun.util.plus
import io.github.addoncommunity.galactifun.util.processSlimefunBlocks
import io.github.seggan.sf4k.location.position
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.block.Block
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.jvm.optionals.getOrNull

class CommandComputer(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>
) : TickingBlock(itemGroup, item, recipeType, recipe) {

    private val counters = Object2IntOpenHashMap<BlockPosition>()

    init {
        addItemHandler(BlockUseHandler(::onRightClick))
        addItemHandler(object : BlockPlaceHandler(false) {
            override fun onPlayerPlace(e: BlockPlaceEvent) {
                rescanRocket(e.block.position)
            }
        })
    }

    override fun tick(b: Block) {
        val pos = b.position
        val counter = counters.mergeInt(pos, 1, Int::plus)
        if (counter % 8 == 0) {
            rescanRocket(pos)
        }
    }

    private fun rescanRocket(pos: BlockPosition) {
        val rocketBlocks = pos.floodSearch { this.checkBlock<RocketEngine>() == null }
        val detected = if (!rocketBlocks.exceededMax) rocketBlocks.found else emptySet()
        val blocks = detected.map(BlockPosition::getBlock)
        val engines = blocks.processSlimefunBlocks<RocketEngine, _> { b ->
            val fuel = b.position.floodSearch { it.checkBlock<FuelTank>() != null }.found.toMutableSet()
            fuel.remove(b.position)
            (this to b.position) to fuel
        }.toMap()
        RocketManager.register(RocketInfo(pos, detected, engines))
    }

    private fun onRightClick(e: PlayerRightClickEvent) {
        val pos = e.clickedBlock.getOrNull()?.position ?: return
        rescanRocket(pos)
        val info = RocketManager.getInfo(pos)!!
        if (info.blocks.isEmpty()) {
            e.player.sendMessage(NamedTextColor.RED + "No rocket detected")
            return
        }
        e.player.sendMessage(NamedTextColor.GOLD + info.info)
    }
}