package io.github.addoncommunity.galactifun.impl.items.rocket

import io.github.addoncommunity.galactifun.api.betteritem.BetterSlimefunItem
import io.github.addoncommunity.galactifun.api.betteritem.Ticker
import io.github.addoncommunity.galactifun.api.blocks.CustomMass
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Gas
import io.github.addoncommunity.galactifun.units.*
import io.github.addoncommunity.galactifun.units.Mass.Companion.kilograms
import io.github.addoncommunity.galactifun.util.bukkit.adjacentFaces
import io.github.addoncommunity.galactifun.util.checkBlock
import io.github.addoncommunity.galactifun.util.general.enumMapOf
import io.github.addoncommunity.galactifun.util.general.mergeMaps
import io.github.addoncommunity.galactifun.util.menu.buildMenu
import io.github.seggan.sf4k.serial.blockstorage.getBlockStorage
import io.github.seggan.sf4k.serial.blockstorage.setBlockStorage
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils
import me.mrCookieSlime.Slimefun.api.BlockStorage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

class FuelTank(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>,
    private val capacity: Volume
) : BetterSlimefunItem(itemGroup, item, recipeType, recipe), CustomMass {

    private companion object {
        private const val INPUT = 4

        private val menu = buildMenu {
            +"c..#i#..."

            'c' means CustomItemStack(Material.WATER_BUCKET, "&fContents")
            '#' means ChestMenuUtils.getInputSlotTexture()
            input('i')
            background('.')
        }
    }

    @Ticker
    private fun tick(b: Block) {
        val menu = BlockStorage.getInventory(b)
        val item = menu.getItemInSlot(INPUT)
        val gasItem = getByItem(item)
        if (gasItem is Gas.Item) {
            val consumed = item.amount.coerceAtMost(8)
            menu.consumeItem(INPUT, consumed)
            val fuel = getFuelLevel(b).toMutableMap()
            fuel.merge(gasItem.gas, consumed.kilograms divToVolume gasItem.gas.liquidDensity, Volume::plus)
            setFuelLevel(b, fuel)
        }

        val fuel = getFuelLevel(b)
        if (fuel.isEmpty()) return

        val result = enumMapOf<Gas, Double>()
        val blocks = adjacentFaces.mapNotNull { b.getRelative(it).checkBlock<FuelTank>() } + b
        var distributable = blocks.size
        val fuels = blocks
            .map(::getFuelLevel)
            .reduce { acc, map -> acc.mergeMaps(map, Volume::plus) }
            .mapValuesTo(enumMapOf()) { (_, amount) -> amount / distributable }

        for (block in blocks) {
            val tank = BlockStorage.check(block) as FuelTank
            val cap = tank.capacity
            val toAdd = if (result.values.sum() > cap.liters) {
                distributable--
                val stuffed = enumMapOf<Gas, Volume>()
                for ((gas, amount) in fuels) {
                    val space = cap - stuffed.values.sum()
                    val toAdd = amount.coerceAtMost(space)
                    stuffed.merge(gas, toAdd, Volume::plus)
                }

                val notStuffed = fuels
                    .mapValues { (gas, amount) -> amount - stuffed.getOrDefault(gas, Volume.ZERO) }
                    .filterValues { it > Volume.ZERO }
                    .mapValues { (_, amount) -> amount / distributable }
                for ((gas, amount) in notStuffed) {
                    fuels.merge(gas, amount, Volume::plus)
                }

                stuffed
            } else {
                fuels
            }

            if (distributable <= 0) break
            setFuelLevel(block, toAdd)
        }

        val contentsItem = menu.getItemInSlot(0)
        val currentFuel = getFuelLevel(b)
        val lore = mutableListOf<Component>()
        for ((gas, amount) in currentFuel) {
            lore += Component.text()
                .color(NamedTextColor.BLUE)
                .content("$gas: %.2s, %.2s".format(amount, amount * gas.liquidDensity))
                .build()
        }
        contentsItem.lore(lore)
    }

    fun getFuelLevel(block: Block): Map<Gas, Volume> {
        return block.getBlockStorage<Map<Gas, Volume>>("fuel") ?: emptyMap()
    }

    fun setFuelLevel(block: Block, fuel: Map<Gas, Volume>) {
        block.setBlockStorage("fuel", fuel)
    }

    override fun getMass(block: Block): Mass = 729.kilograms // based on a 1 m^3 tank with 0.5 m thick walls of aluminum

    override fun getWetMass(block: Block): Mass {
        val fuelMass = getFuelLevel(block).toList()
            .unitSumOf { (gas, amount) -> gas.liquidDensity * amount }
        return getMass(block) + fuelMass
    }

    override fun beforeRegister() {
        menu.apply(this)
    }
}