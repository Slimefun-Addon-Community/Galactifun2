package io.github.addoncommunity.galactifun.scripting.dsl.gen

import io.github.addoncommunity.galactifun.scripting.PlanetDsl
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.RandomizedSet
import org.bukkit.Material


typealias BlockProvider = SimplePerlinBuilder.GenInfo.() -> Material

@PlanetDsl
class SimpleBlocksBuilder {

    var top = mutableListOf<BlockProvider>()
    var bottom = mutableListOf<BlockProvider>()
    var middle: BlockProvider = { Material.AIR }

    private fun addN(n: Int, list: MutableList<BlockProvider>, block: BlockProvider) {
        repeat(n) {
            list.add(block)
        }
    }

    infix fun BlockProvider.top(n: Int) = addN(n, top, this)

    infix fun Material.top(n: Int) = addN(n, top) { this@top }

    infix fun BlockProvider.bottom(n: Int) = addN(n, bottom, this)

    infix fun Material.bottom(n: Int) = addN(n, bottom) { this@bottom }

    @PlanetDsl
    class RandomBuilder {
        internal val selector = RandomizedSet<Material>()

        infix fun Material.withWeight(weight: Float) {
            selector.add(this, weight)
        }
    }
}

fun SimpleBlocksBuilder.fillInRestWith(block: BlockProvider) {
    middle = block
}

fun SimpleBlocksBuilder.fillInRestWith(material: Material) {
    middle = { material }
}

fun SimpleBlocksBuilder.random(block: SimpleBlocksBuilder.RandomBuilder.() -> Unit): BlockProvider {
    val builder = SimpleBlocksBuilder.RandomBuilder()
    builder.block()
    return {
        builder.selector.getRandom(random)
    }
}