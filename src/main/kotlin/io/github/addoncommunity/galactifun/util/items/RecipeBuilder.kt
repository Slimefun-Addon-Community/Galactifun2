package io.github.addoncommunity.galactifun.util.items

import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap
import org.bukkit.inventory.ItemStack

class RecipeBuilder {

    private val recipe: Array<String> = Array(3) { " ".repeat(3) }
    private var row = 0

    private val charMap = Char2ObjectOpenHashMap<ItemStack?>().apply {
        put(' ', null)
    }

    operator fun String.unaryPlus() {
        require(length == 3) { "Recipe must be 3x3" }
        require(row < 3) { "Recipe must be 3x3" }
        recipe[row++] = this
    }

    infix fun Char.means(item: ItemStack?) {
        charMap.put(this, item)
    }

    fun build(): Array<out ItemStack?> {
        return recipe.flatMap { row -> row.map { charMap[it] } }.toTypedArray()
    }
}

inline fun buildRecipe(init: RecipeBuilder.() -> Unit): Array<out ItemStack?> {
    return RecipeBuilder().apply(init).build()
}