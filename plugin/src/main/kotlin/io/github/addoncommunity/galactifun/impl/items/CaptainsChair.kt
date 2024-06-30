package io.github.addoncommunity.galactifun.impl.items

import io.github.addoncommunity.galactifun.impl.items.abstract.Seat
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CaptainsChair(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>
) : Seat(itemGroup, item, recipeType, recipe) {

    override fun onSit(p: Player, b: Block) {
        p.sendMessage("You are now sitting in the Captain's Chair")
    }
}