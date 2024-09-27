package io.github.addoncommunity.galactifun.impl.items

import io.github.addoncommunity.galactifun.api.blocks.BasicMassedBlock
import io.github.addoncommunity.galactifun.api.blocks.HeatResistant
import io.github.addoncommunity.galactifun.api.blocks.ReentryBurnable
import io.github.addoncommunity.galactifun.impl.GalactifunItems
import io.github.addoncommunity.galactifun.units.Mass.Companion.kilograms
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import me.mrCookieSlime.Slimefun.api.BlockStorage
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

class HeatShield(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>
) : BasicMassedBlock(itemGroup, item, recipeType, recipe, 775.kilograms), HeatResistant, ReentryBurnable {
    override fun convert(b: Block) {
        b.type = GalactifunItems.USED_HEAT_SHIELD.type
        BlockStorage.addBlockInfo(b, "id", GalactifunItems.USED_HEAT_SHIELD.itemId)
    }
}

class BurntHeatShield(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>
) : BasicMassedBlock(itemGroup, item, recipeType, recipe, 500.kilograms)