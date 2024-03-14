package io.github.addoncommunity.galactifun.blocks

import io.github.addoncommunity.galactifun.units.Mass
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

open class BasicMassedBlock(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>,
    private val mass: Mass
) : SlimefunItem(itemGroup, item, recipeType, recipe), CustomMass {

    init {
        addItemHandler(BlockUseHandler {
            it.cancel()
        })
    }

    override fun getMass(block: Block): Mass = mass
}