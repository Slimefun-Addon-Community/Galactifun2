package io.github.addoncommunity.galactifun.util.items

import io.github.addoncommunity.galactifun.pluginInstance
import io.github.addoncommunity.galactifun.util.RequiredProperty
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils
import org.bukkit.inventory.ItemStack

class ItemBuilder(
    private val constructor: ((
        ItemGroup,
        SlimefunItemStack,
        RecipeType,
        Array<out ItemStack?>
    ) -> SlimefunItem)?
) {

    var name: String by RequiredProperty()
    var material: MaterialType by RequiredProperty()
    var id: String by RequiredProperty(setter = { "AUTOMATION_" + it.uppercase() })

    var category: ItemGroup by RequiredProperty()
    var recipeType: RecipeType by RequiredProperty()
    var recipe: Array<out ItemStack?> by RequiredProperty()

    private val lore = mutableListOf<String>()

    operator fun String.unaryPlus() {
        lore += this
    }

    fun build(): SlimefunItemStack {
        val sfi = SlimefunItemStack(
            id,
            material.convert(),
            name,
            *lore.toTypedArray()
        )
        constructor?.let { it(category, sfi, recipeType, recipe).register(pluginInstance) }
        return sfi
    }
}


sealed interface MaterialType {

    fun convert(): org.bukkit.inventory.ItemStack

    class Material(private val material: org.bukkit.Material) : MaterialType {
        override fun convert() = ItemStack(material)
    }

    class ItemStack(private val itemStack: org.bukkit.inventory.ItemStack) : MaterialType {
        override fun convert() = itemStack
    }

    class Head(private val texture: String) : MaterialType {
        override fun convert() = SlimefunUtils.getCustomHead(texture)
    }
}

inline fun buildSlimefunItem(
    noinline constructor: ((
        ItemGroup,
        SlimefunItemStack,
        RecipeType,
        Array<out ItemStack?>
    ) -> SlimefunItem)? = ::SlimefunItem,
    builder: ItemBuilder.() -> Unit
): SlimefunItemStack {
    return ItemBuilder(constructor).apply(builder).build()
}