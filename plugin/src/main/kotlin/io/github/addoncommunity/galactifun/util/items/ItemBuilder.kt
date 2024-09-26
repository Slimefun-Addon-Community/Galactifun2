package io.github.addoncommunity.galactifun.util.items

import io.github.addoncommunity.galactifun.Galactifun2
import io.github.addoncommunity.galactifun.util.bukkit.miniMessageToLegacy
import io.github.addoncommunity.galactifun.util.general.RequiredProperty
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class ItemBuilder {

    var name: String by RequiredProperty()
    var material: MaterialType by RequiredProperty()
    var id: String by RequiredProperty(setter = { "GF2_" + it.uppercase() })

    var category: ItemGroup by RequiredProperty()
    var recipeType: RecipeType by RequiredProperty()
    var recipe: Array<out ItemStack?> by RequiredProperty()

    private val lore = mutableListOf<String>()

    operator fun String.unaryPlus() {
        lore += this.miniMessageToLegacy()
    }

    fun build(clazz: KClass<out SlimefunItem>, vararg otherArgs: Any?): SlimefunItemStack {
        val sfi = SlimefunItemStack(
            id,
            material.convert(),
            name.miniMessageToLegacy(),
            *lore.toTypedArray()
        )
        val constructor = clazz.primaryConstructor ?: error("Primary constructor not found for $clazz")
        constructor.call(category, sfi, recipeType, recipe, *otherArgs).register(Galactifun2)
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

val Material.materialType get() = MaterialType.Material(this)
val ItemStack.materialType get() = MaterialType.ItemStack(this)

inline fun <reified I : SlimefunItem> buildSlimefunItem(
    vararg otherArgs: Any?,
    builder: ItemBuilder.() -> Unit
): SlimefunItemStack {
    return ItemBuilder().apply(builder).build(I::class, *otherArgs)
}

inline fun buildSlimefunItem(
    builder: ItemBuilder.() -> Unit
): SlimefunItemStack {
    return ItemBuilder().apply(builder).build(SlimefunItem::class)
}