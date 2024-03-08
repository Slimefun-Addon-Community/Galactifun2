package io.github.addoncommunity.galactifun.impl

import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Gas
import io.github.addoncommunity.galactifun.impl.items.CommandComputer
import io.github.addoncommunity.galactifun.impl.items.FuelTank
import io.github.addoncommunity.galactifun.pluginInstance
import io.github.addoncommunity.galactifun.util.items.MaterialType
import io.github.addoncommunity.galactifun.util.items.buildSlimefunItem
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import org.bukkit.Material

object GalactifunItems {

    val COMMAND_COMPUTER = buildSlimefunItem<CommandComputer> {
        category = GalactifunCategories.ROCKET_COMPONENTS
        id = "COMMAND_COMPUTER"
        name = "&fCommand Computer"
        material = MaterialType.Material(Material.CHISELED_QUARTZ_BLOCK)
        recipeType = RecipeType.NULL
        recipe = emptyArray()
    }

    val FUEL_TANK_I = buildSlimefunItem<FuelTank>(1000.0) {
        category = GalactifunCategories.ROCKET_COMPONENTS
        id = "FUEL_TANK_I"
        name = "&fFuel Tank I"
        material = MaterialType.Material(Material.IRON_BLOCK)
        recipeType = RecipeType.NULL
        recipe = emptyArray()
    }

    init {
        for (gas in Gas.entries) {
            if (gas.item == null) continue
            Gas.Item(GalactifunCategories.GASES, gas, RecipeType.NULL, emptyArray()).register(pluginInstance)
        }
    }
}