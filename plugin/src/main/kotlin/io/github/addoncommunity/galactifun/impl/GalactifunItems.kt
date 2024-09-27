package io.github.addoncommunity.galactifun.impl

import io.github.addoncommunity.galactifun.Galactifun2
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Gas
import io.github.addoncommunity.galactifun.impl.items.*
import io.github.addoncommunity.galactifun.units.Force.Companion.kilonewtons
import io.github.addoncommunity.galactifun.units.Volume.Companion.liters
import io.github.addoncommunity.galactifun.util.items.buildSlimefunItem
import io.github.addoncommunity.galactifun.util.items.materialType
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import org.bukkit.Material
import kotlin.time.Duration.Companion.seconds

@Suppress("unused")
object GalactifunItems {

    val SLIMEFUN_STRUCTURE_BLOCK = buildSlimefunItem<SlimefunStructureBlock> {
        category = GalactifunCategories.HIDDEN
        id = "SLIMEFUN_STRUCTURE_BLOCK"
        name = "Slimefun Structure Block"
        material = Material.STRUCTURE_BLOCK.materialType
        recipeType = RecipeType.NULL
        recipe = emptyArray()
    }

    val COMMAND_COMPUTER = buildSlimefunItem<CommandComputer> {
        category = GalactifunCategories.ROCKET_COMPONENTS
        id = "COMMAND_COMPUTER"
        name = "Command Computer"
        material = Material.CHISELED_QUARTZ_BLOCK.materialType
        recipeType = RecipeType.NULL
        recipe = emptyArray()

        +"The core for any rocket"
    }

    val HEAT_SHIELD = buildSlimefunItem<HeatShield> {
        category = GalactifunCategories.ROCKET_COMPONENTS
        id = "HEAT_SHIELD"
        name = "Heat Shield"
        material = Material.NETHER_BRICK_SLAB.materialType
        recipeType = RecipeType.NULL
        recipe = emptyArray()

        +"Protects the rocket against the"
        +"temperatures of reentry"
        +""
        +"<white><bold>Heat resistant"
    }

    val USED_HEAT_SHIELD = buildSlimefunItem<BurntHeatShield> {
        category = GalactifunCategories.ROCKET_COMPONENTS
        id = "USED_HEAT_SHIELD"
        name = "<red>Used Heat Shield"
        material = Material.DEEPSLATE_TILE_SLAB.materialType
        recipeType = RecipeType.NULL
        recipe = emptyArray()

        +"A heat shield that requires replacement"
    }

    val CAPTAINS_CHAIR = buildSlimefunItem<CaptainsChair> {
        category = GalactifunCategories.ROCKET_COMPONENTS
        id = "CAPTAINS_CHAIR"
        name = "Captain's Chair"
        material = Material.OAK_STAIRS.materialType
        recipeType = RecipeType.NULL
        recipe = emptyArray()

        +"The captain's chair"
    }

    val FUEL_TANK_I = buildSlimefunItem<FuelTank>(1000.liters) {
        category = GalactifunCategories.ROCKET_COMPONENTS
        id = "FUEL_TANK_I"
        name = "TNK-1000 \"Big Boy\""
        material = Material.IRON_BLOCK.materialType
        recipeType = RecipeType.NULL
        recipe = emptyArray()

        +"Definitely not a tin can"
        +""
        +"Manufacturer: Found by the side of the road"
        +"<yellow>Capacity: 1000 L"
        +""
        +"<white>Heat resistant"
    }

    val ROCKET_ENGINE_I = buildSlimefunItem<RocketEngine>(30000.seconds, 100.kilonewtons, Gas.HYDROGEN) {
        category = GalactifunCategories.ROCKET_COMPONENTS
        id = "ROCKET_ENGINE_I"
        name = """BMR-50 "Lil' Boomer""""
        material = Material.FURNACE.materialType
        recipeType = RecipeType.NULL
        recipe = emptyArray()

        +"Not to be used for grilling burgers"
        +""
        +"Manufacturer: Boomer & Bros. Explosives, Inc."
        +"<yellow>Thrust: 100 kN"
        +"<yellow>Specific impulse: 30,000 s"
        +"<yellow>Fuel: Hydrogen"
        +""
        +"<white>Heat resistant"
    }

    val ROCKET_ENGINE_II = buildSlimefunItem<RocketEngine>(20000.seconds, 300.kilonewtons, Gas.HYDROGEN) {
        category = GalactifunCategories.ROCKET_COMPONENTS
        id = "ROCKET_ENGINE_II"
        name = """BMR-100 "Normal Boomer""""
        material = Material.FURNACE.materialType
        recipeType = RecipeType.NULL
        recipe = emptyArray()

        +"Your standard junkyard rocket engine"
        +""
        +"Manufacturer: Boomer & Bros. Explosives, Inc."
        +"<yellow>Thrust: 300 kN"
        +"<yellow>Specific impulse: 20,000 s"
        +"<yellow>Fuel: Hydrogen"
        +""
        +"<white>Heat resistant"
    }

    init {
        for (gas in Gas.entries) {
            if (gas.item == null) continue
            Gas.Item(GalactifunCategories.GASES, gas, RecipeType.NULL, emptyArray()).register(Galactifun2)
        }
    }
}