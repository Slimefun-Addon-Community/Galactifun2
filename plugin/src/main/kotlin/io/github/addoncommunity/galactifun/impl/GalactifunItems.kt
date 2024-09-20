package io.github.addoncommunity.galactifun.impl

import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Gas
import io.github.addoncommunity.galactifun.impl.items.*
import io.github.addoncommunity.galactifun.pluginInstance
import io.github.addoncommunity.galactifun.units.Force.Companion.kilonewtons
import io.github.addoncommunity.galactifun.units.Volume.Companion.liters
import io.github.addoncommunity.galactifun.util.items.MaterialType
import io.github.addoncommunity.galactifun.util.items.buildSlimefunItem
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import org.bukkit.Material
import kotlin.time.Duration.Companion.seconds

@Suppress("unused")
object GalactifunItems {

    val SLIMEFUN_STRUCTURE_BLOCK = buildSlimefunItem<SlimefunStructureBlock> {
        category = GalactifunCategories.HIDDEN
        id = "SLIMEFUN_STRUCTURE_BLOCK"
        name = "<white>Slimefun Structure Block"
        material = MaterialType.Material(Material.STRUCTURE_BLOCK)
        recipeType = RecipeType.NULL
        recipe = emptyArray()
    }

    val COMMAND_COMPUTER = buildSlimefunItem<CommandComputer> {
        category = GalactifunCategories.ROCKET_COMPONENTS
        id = "COMMAND_COMPUTER"
        name = "<white>Command Computer"
        material = MaterialType.Material(Material.CHISELED_QUARTZ_BLOCK)
        recipeType = RecipeType.NULL
        recipe = emptyArray()

        +"<gray>The core for any rocket"
    }

    val CAPTAINS_CHAIR = buildSlimefunItem<CaptainsChair> {
        category = GalactifunCategories.ROCKET_COMPONENTS
        id = "CAPTAINS_CHAIR"
        name = "<white>Captain's Chair"
        material = MaterialType.Material(Material.OAK_STAIRS)
        recipeType = RecipeType.NULL
        recipe = emptyArray()

        +"<gray>The Captain's Chair"
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

    val ROCKET_ENGINE_I = buildSlimefunItem<RocketEngine>(30000.seconds, 100.kilonewtons, Gas.HYDROGEN) {
        category = GalactifunCategories.ROCKET_COMPONENTS
        id = "ROCKET_ENGINE_I"
        name = """<white>BMR-50 "Lil' Boomer""""
        material = MaterialType.Material(Material.FURNACE)
        recipeType = RecipeType.NULL
        recipe = emptyArray()

        +"<gray>Not to be used for grilling burgers"
        +""
        +"<gray>Manufacturer: Boomer & Bros. Explosives, Inc."
        +"<yellow>Thrust: 100 kN"
        +"<yellow>Specific impulse: 30,000 s"
        +"<yellow>Fuel: Hydrogen"
    }

    val ROCKET_ENGINE_II = buildSlimefunItem<RocketEngine>(20000.seconds, 300.kilonewtons, Gas.HYDROGEN) {
        category = GalactifunCategories.ROCKET_COMPONENTS
        id = "ROCKET_ENGINE_II"
        name = """<white>BMR-100 "Normal Boomer""""
        material = MaterialType.Material(Material.FURNACE)
        recipeType = RecipeType.NULL
        recipe = emptyArray()

        +"<gray>Your standard junkyard rocket engine"
        +""
        +"<gray>Manufacturer: Boomer & Bros. Explosives, Inc."
        +"<yellow>Thrust: 300 kN"
        +"<yellow>Specific impulse: 20,000 s"
        +"<yellow>Fuel: Hydrogen"
    }

    init {
        for (gas in Gas.entries) {
            if (gas.item == null) continue
            Gas.Item(GalactifunCategories.GASES, gas, RecipeType.NULL, emptyArray()).register(pluginInstance)
        }
    }
}