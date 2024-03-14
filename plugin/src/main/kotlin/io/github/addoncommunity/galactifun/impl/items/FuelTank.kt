package io.github.addoncommunity.galactifun.impl.items

import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Gas
import io.github.addoncommunity.galactifun.blocks.CustomMass
import io.github.addoncommunity.galactifun.units.Mass
import io.github.addoncommunity.galactifun.units.Mass.Companion.kilograms
import io.github.addoncommunity.galactifun.units.Volume
import io.github.addoncommunity.galactifun.units.Volume.Companion.liters
import io.github.addoncommunity.galactifun.units.sumBy
import io.github.addoncommunity.galactifun.units.times
import io.github.addoncommunity.galactifun.util.adjacentFaces
import io.github.addoncommunity.galactifun.util.checkBlock
import io.github.addoncommunity.galactifun.util.general.enumMapOf
import io.github.addoncommunity.galactifun.util.general.mergeMaps
import io.github.addoncommunity.galactifun.util.items.TickingBlock
import io.github.addoncommunity.galactifun.util.items.buildMenu
import io.github.seggan.sf4k.serial.*
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import me.mrCookieSlime.Slimefun.api.BlockStorage
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

class FuelTank(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>,
    private val capacity: Volume
) : TickingBlock(itemGroup, item, recipeType, recipe), CustomMass {

    private companion object {
        private const val INPUT = 4

        private val menu = buildMenu {
            numRows = 1
            input(INPUT, 0).addBorder()
        }

        private val fuelDataType = MapBlockStorageDataType(
            EnumBlockStorageDataType(Gas::class),
            BlockStorageDataType.DOUBLE
        )
    }

    override fun tick(b: Block) {
        val menu = BlockStorage.getInventory(b)
        val item = menu.getItemInSlot(INPUT)
        val gasItem = getByItem(item)
        if (gasItem is Gas.Item) {
            val consumed = item.amount.coerceAtMost(8)
            menu.consumeItem(INPUT, consumed)
            val fuel = getFuelLevel(b).toMutableMap()
            fuel.merge(gasItem.gas, consumed.liters, Volume::plus)
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
                    val space = cap - stuffed.values.sumOf { it.liters }.liters
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
    }

    fun getFuelLevel(block: Block): Map<Gas, Volume> {
        return block.getBlockStorage("fuel", fuelDataType)?.mapValues { it.value.liters } ?: emptyMap()
    }

    fun setFuelLevel(block: Block, fuel: Map<Gas, Volume>) {
        block.setBlockStorage("fuel", fuel.mapValues { it.value.liters }, fuelDataType)
    }

    override fun getMass(block: Block): Mass = 729.kilograms // based on a 1 m^3 tank with 0.5 m thick walls of aluminum

    override fun getWetMass(block: Block): Mass {
        val fuelMass = getFuelLevel(block).toList()
            .sumBy { (gas, amount) -> gas.liquidDensity * amount }
        return getMass(block) + fuelMass
    }

    override fun preRegister() {
        menu.applyOn(this)
    }
}