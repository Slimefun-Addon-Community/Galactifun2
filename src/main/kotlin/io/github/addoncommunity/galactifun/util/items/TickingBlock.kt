package io.github.addoncommunity.galactifun.util.items

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

abstract class TickingBlock(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>
) : SlimefunItem(itemGroup, item, recipeType, recipe) {

    protected open val tickSync = true

    init {
        addItemHandler(object : BlockTicker() {
            override fun isSynchronized() = tickSync
            override fun tick(b: Block, item: SlimefunItem, data: Config) = tick(b)
        })
    }

    protected abstract fun tick(b: Block)
}