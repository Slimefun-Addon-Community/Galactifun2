package io.github.addoncommunity.galactifun.impl

import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Gas
import io.github.addoncommunity.galactifun.impl.items.CommandComputer
import io.github.addoncommunity.galactifun.impl.items.FuelTank
import io.github.addoncommunity.galactifun.impl.items.RocketEngine
import io.github.addoncommunity.galactifun.pluginInstance
import io.github.addoncommunity.galactifun.units.Force.Companion.kilonewtons
import io.github.addoncommunity.galactifun.units.Volume.Companion.liters
import io.github.addoncommunity.galactifun.util.items.MaterialType
import io.github.addoncommunity.galactifun.util.items.buildSlimefunItem
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import org.bukkit.Material
import kotlin.time.Duration.Companion.seconds

object GalactifunItems {

    val COMMAND_COMPUTER = buildSlimefunItem<CommandComputer> {
        category = GalactifunCategories.ROCKET_COMPONENTS
        id = "COMMAND_COMPUTER"
        name = "<white>Command Computer"
        material = MaterialType.Material(Material.CHISELED_QUARTZ_BLOCK)
        recipeType = RecipeType.NULL
        recipe = emptyArray()

        +"&7The core for any rocket"
    }

    val FUEL_TANK_I = buildSlimefunItem<FuelTank>(1000.liters) {
        category = GalactifunCategories.ROCKET_COMPONENTS
        id = "FUEL_TANK_I"
        name = "<white>TNK-1000 \"Big Boy\""
        material = MaterialType.Material(Material.IRON_BLOCK)
        recipeType = RecipeType.NULL
        recipe = emptyArray()

        +"<gray>Definitely not a tin can"
        +""
        +"<gray>Manufacturer: Found by the side of the road"
        +"<yellow>Capacity: 1000 L"
    }

    val ROCKET_ENGINE_I = buildSlimefunItem<RocketEngine>(300.seconds, 100.kilonewtons, Gas.HYDROGEN) {
        category = GalactifunCategories.ROCKET_COMPONENTS
        id = "ROCKET_ENGINE_I"
        name = "<white>BMR-50 \"Lil' Boomer\""
        material = MaterialType.Material(Material.FURNACE)
        recipeType = RecipeType.NULL
        recipe = emptyArray()

        +"<gray>Not to be used for grilling burgers"
        +""
        +"<gray>Manufacturer: Boomer & Bros. Explosives, Inc."
        +"<yellow>Thrust: 100 kN"
        +"<yellow>Specific impulse: 300 s"
        +"<yellow>Fuel: Hydrogen"
    }

    init {
        for (gas in Gas.entries) {
            if (gas.item == null) continue
            Gas.Item(GalactifunCategories.GASES, gas, RecipeType.NULL, emptyArray()).register(pluginInstance)
        }
    }
}