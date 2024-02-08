package io.github.addoncommunity.galactifun.base.items

import io.github.addoncommunity.galactifun.util.BlockTicker
import io.github.addoncommunity.galactifun.util.floodSearch
import io.github.seggan.kfun.location.position
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import io.github.thebusybiscuit.slimefun4.utils.tags.SlimefunTag
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import java.util.*

class CommandComputer(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack>
) : SlimefunItem(itemGroup, item, recipeType, recipe) {

    private val allRockets = mutableMapOf<BlockPosition, Set<BlockPosition>>()
    private val counters = Object2IntOpenHashMap<BlockPosition>()

    init {
        addItemHandler(BlockTicker(true, ::tick))
    }

    private fun tick(block: Block) {
        val pos = block.position
        val counter = counters.mergeInt(pos, 1, Int::plus)
        if (counter % 4 == 0) {
            rescanRocket(pos)
        }
    }

    private fun rescanRocket(pos: BlockPosition) {
        val rocketBlocks = pos.floodSearch { it.type !in notAllowedBlocks }
        if (!rocketBlocks.exceededMax) {
            allRockets[pos] = rocketBlocks.found
        } else {
            allRockets[pos] = emptySet()
        }
    }
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