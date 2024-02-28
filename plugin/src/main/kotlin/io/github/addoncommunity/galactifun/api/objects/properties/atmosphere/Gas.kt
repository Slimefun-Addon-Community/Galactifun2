package io.github.addoncommunity.galactifun.api.objects.properties.atmosphere

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.implementation.items.blocks.UnplaceableBlock
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils
import org.bukkit.inventory.ItemStack

/**
 * @property liquidDensity density of the gas in the liquid state, in kg/l
 */
enum class Gas(texture: String?, val liquidDensity: Double) {
    OXYGEN("5b3ad76aadb80ecf4b4cdbe76b8704b0f2dc090b49b65c36d87ed879f1065ef2", 1.146),
    NITROGEN("5b3ad76aadb80ecf4b4cdbe76b8704b0f2dc090b49b65c36d87ed879f1065ef2", 0.811),
    CARBON_DIOXIDE("5b3ad76aadb80ecf4b4cdbe76b8704b0f2dc090b49b65c36d87ed879f1065ef2", 0.002),
    WATER("5b3ad76aadb80ecf4b4cdbe76b8704b0f2dc090b49b65c36d87ed879f1065ef2", 0.997),
    HELIUM("93dfa904fe3d0306666a573c22eec1dd0a8051e32a38ea2d19c4b5867e232a49", 0.141),
    ARGON("ea005531b6167a86fb09d6c0f3db60f2650162d0656c2908d07b377111d8f2a2", 1.401),
    METHANE("ea005531b6167a86fb09d6c0f3db60f2650162d0656c2908d07b377111d8f2a2", 0.424),
    // Using hexane for reference density
    HYDROCARBONS("725691372e0734bfb57bb03690490661a83f053a3488860df3436ce1caa24d11", 0.655),
    HYDROGEN("725691372e0734bfb57bb03690490661a83f053a3488860df3436ce1caa24d11", 0.072),
    SULFUR("c7a1ece691ad28d17bbbcecb22270c85e1c9581485806264c676de67c272e2d0", 1.819),
    AMMONIA("c7a1ece691ad28d17bbbcecb22270c85e1c9581485806264c676de67c272e2d0", 0.683),
    OTHER(null, Double.NaN);

    val item = texture?.let {
        SlimefunItemStack(
            "ATMOSPHERIC_GAS_$name",
            SlimefunUtils.getCustomHead(texture),
            "&f$this Gas Canister",
            "",
            "&f&oTexture by Sefiraat"
        )
    }

    override fun toString(): String = ChatUtils.humanize(name)

    class Item internal constructor(
        itemGroup: ItemGroup,
        val gas: Gas,
        recipeType: RecipeType,
        recipe: Array<out ItemStack>
    ) : UnplaceableBlock(itemGroup, requireNotNull(gas.item) { "Gas $gas has no item" }, recipeType, recipe)
}