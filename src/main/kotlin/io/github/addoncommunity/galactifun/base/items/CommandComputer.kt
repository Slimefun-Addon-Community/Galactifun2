package io.github.addoncommunity.galactifun.base.items

import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Gas
import io.github.addoncommunity.galactifun.api.rockets.RocketInfo
import io.github.addoncommunity.galactifun.core.managers.RocketManager
import io.github.addoncommunity.galactifun.util.floodSearch
import io.github.addoncommunity.galactifun.util.items.TickingBlock
import io.github.addoncommunity.galactifun.util.mergeMaps
import io.github.addoncommunity.galactifun.util.plus
import io.github.seggan.kfun.location.position
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import io.github.thebusybiscuit.slimefun4.utils.tags.SlimefunTag
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import me.mrCookieSlime.Slimefun.api.BlockStorage
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block
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
    }

    override fun tick(b: Block) {
        val pos = b.position
        val counter = counters.mergeInt(pos, 1, Int::plus)
        if (counter % 4 == 0) {
            rescanRocket(pos)
        }
    }

    private fun rescanRocket(pos: BlockPosition) {
        val rocketBlocks = pos.floodSearch { it.type !in notAllowedBlocks }
        val detected = if (!rocketBlocks.exceededMax) rocketBlocks.found else emptySet()
        val blocks = detected.map(BlockPosition::getBlock)
        val fuel = blocks.processSlimefunBlocks(FuelTank::getFuelLevel)
            .fold(emptyMap<Gas, Double>()) { acc, map -> acc.mergeMaps(map, Double::plus) }
        RocketManager.register(RocketInfo(pos, detected, fuel))
    }

    private fun onRightClick(e: PlayerRightClickEvent) {
        val pos = e.clickedBlock.getOrNull()?.position ?: return
        rescanRocket(pos)
        val info = RocketManager.getInfo(pos)!!
        if (info.blocks.isEmpty()) {
            e.player.sendMessage(NamedTextColor.RED + "No rocket detected")
            return
        }
        val infoString = buildString {
            appendLine("Fuel:")
            for ((gas, amount) in info.fuel) {
                append(" ".repeat(4))
                append(gas)
                append(": ")
                append("%.2f".format(amount))
                append(" liters, ")
                append("%.2f".format(amount * gas.liquidDensity))
                append(" kg")
                appendLine()
            }
        }
        e.player.sendMessage(NamedTextColor.GOLD + infoString)
    }

    private val notAllowedBlocks = EnumSet.noneOf(Material::class.java).apply {
        addAll(Tag.DIRT.values)
        addAll(Tag.SAND.values)
        add(Material.GRAVEL)
        addAll(Tag.CONCRETE_POWDER.values)
        add(Material.AIR)
        add(Material.WATER)
        add(Material.LAVA)
        add(Material.FIRE)
        addAll(SlimefunTag.FLUID_SENSITIVE_MATERIALS.values)
        addAll(SlimefunTag.UNBREAKABLE_MATERIALS.values)
    }
}

private inline fun <reified S : SlimefunItem, T> Iterable<Block>.processSlimefunBlocks(
    processor: S.(Block) -> T
): List<T> {
    val result = mutableListOf<T>()
    for (b in this) {
        val item = BlockStorage.check(b)
        if (item is S) {
            result.add(item.processor(b))
        }
    }
    return result
}