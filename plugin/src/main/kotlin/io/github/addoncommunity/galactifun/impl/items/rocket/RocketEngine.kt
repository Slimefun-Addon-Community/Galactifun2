package io.github.addoncommunity.galactifun.impl.items.rocket

import io.github.addoncommunity.galactifun.api.blocks.HeatResistant
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Gas
import io.github.addoncommunity.galactifun.units.Force
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler
import org.bukkit.inventory.ItemStack
import kotlin.time.Duration

class RocketEngine(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>,
    val specificImpulse: Duration,
    val thrust: Force,
    val fuel: Gas
) : SlimefunItem(itemGroup, item, recipeType, recipe), HeatResistant {

    init {
        addItemHandler(BlockUseHandler {
            it.cancel()
        })
    }
}