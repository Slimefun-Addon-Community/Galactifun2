package io.github.addoncommunity.galactifun.impl.items

import io.github.addoncommunity.galactifun.api.betteritem.BetterSlimefunItem
import io.github.addoncommunity.galactifun.api.betteritem.ItemHandler
import io.github.addoncommunity.galactifun.api.betteritem.Ticker
import io.github.addoncommunity.galactifun.api.objects.WorldType
import io.github.addoncommunity.galactifun.api.objects.planet.PlanetaryWorld
import io.github.addoncommunity.galactifun.api.rockets.RocketInfo
import io.github.addoncommunity.galactifun.impl.GalactifunHeads
import io.github.addoncommunity.galactifun.impl.items.abstract.Seat
import io.github.addoncommunity.galactifun.impl.managers.PlanetManager
import io.github.addoncommunity.galactifun.impl.managers.RocketManager
import io.github.addoncommunity.galactifun.util.*
import io.github.addoncommunity.galactifun.util.bukkit.*
import io.github.addoncommunity.galactifun.util.menu.buildMenu
import io.github.seggan.sf4k.extensions.position
import io.github.seggan.sf4k.serial.blockstorage.getBlockStorage
import io.github.seggan.sf4k.serial.blockstorage.setBlockStorage
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu
import me.mrCookieSlime.Slimefun.api.BlockStorage
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.jvm.optionals.getOrNull

class CommandComputer(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>
) : BetterSlimefunItem(itemGroup, item, recipeType, recipe) {

    private val counters = Object2IntOpenHashMap<BlockPosition>()

    companion object : Listener {
        val SERIALIZED_BLOCK_KEY = "serialized_block".key()
    }

    @Ticker
    private fun tick(b: Block) {
        val pos = b.position
        val counter = counters.mergeInt(pos, 1, Int::plus)
        if (counter % 8 == 0) {
            rescanRocket(pos)
        }
    }

    @ItemHandler(BlockPlaceHandler::class)
    private fun onPlace(e: BlockPlaceEvent) {
        rescanRocket(e.block.position)
    }

    @ItemHandler(BlockBreakHandler::class)
    private fun onBreak(e: BlockBreakEvent, item: ItemStack, drops: MutableList<ItemStack>) {
        RocketManager.unregister(RocketManager.getInfo(e.block.position) ?: return)
    }

    private fun rescanRocket(pos: BlockPosition) {
        val rocketBlocks = pos.floodSearch { this.checkBlock<RocketEngine>() == null }
        val detected = if (!rocketBlocks.exceededMax) rocketBlocks.found else emptySet()
        val blocks = detected.map(BlockPosition::getBlock)
        blocks.processSlimefunBlocks<CaptainsChair, Unit> {
            it.setBlockStorage("rocket", pos)
        }
        val engines = blocks.processSlimefunBlocks<RocketEngine, _> { b ->
            val fuel = b.position.floodSearch { it.checkBlock<FuelTank>() != null }.found.toMutableSet()
            fuel.remove(b.position)
            (this to b.position) to fuel
        }.toMap()
        RocketManager.register(RocketInfo(pos, detected, engines))
    }

    @ItemHandler(BlockUseHandler::class)
    private fun onRightClick(e: PlayerRightClickEvent) {
        e.cancel()
        val p = e.player
        val block = e.clickedBlock.getOrNull() ?: return
        val pos = block.position
        rescanRocket(pos)
        val info = RocketManager.getInfo(pos)!!
        if (info.blocks.isEmpty()) {
            p.sendMessage(NamedTextColor.RED + "No rocket detected")
            return
        }
        getMenu(block, info).open(p)
        val seat = Seat.getSitting(p)
        if (seat != null
            && BlockStorage.check(seat) is CaptainsChair
            && seat.getBlockStorage<BlockPosition>("rocket") == pos
        ) {
            if (p.world == PlanetManager.spaceWorld) {
            } else {
                RocketManager.launchRocket(p, info, seat)
            }
        } else {
            e.player.sendMessage(NamedTextColor.GOLD + info.info)
        }
    }

    @OptIn(ExperimentalContracts::class)
    private fun canLaunch(seat: Location?, info: RocketInfo): Boolean {
        contract {
            returns(true) implies (seat != null)
        }
        return seat != null
                && BlockStorage.check(seat) is CaptainsChair
                && seat.getBlockStorage<BlockPosition>("rocket") == info.commandComputer
    }

    private fun getMenu(b: Block, rocket: RocketInfo): ChestMenu {
        val worldType = WorldType.fromLocation(b.location)
        return buildMenu {
            +"..i.l.t.."

            background('.')

            'i' means item {
                name = "Rocket Information"
                material = GalactifunHeads.INFO.materialType

                +""
                for (line in rocket.info.lines()) {
                    +line
                }
                if (worldType is WorldType.Space) {
                    +""
                    +"Orbiting: ${worldType.orbiting.name}"
                }
            }

            'l' means item {
                material = GalactifunHeads.ROCKET.materialType

                when (worldType) {
                    is WorldType.Planet -> {
                        name = "Launch Rocket"

                        val planet = worldType.planet
                        val cost = planet.orbitCost

                        +""
                        +"<gold>Delta-V required to launch to orbit: $cost m/s"
                        if (rocket.stages.first().deltaV < cost) {
                            +""
                            +"<red><bold>The rocket does not have enough delta-v to reach orbit"
                        }
                        if (rocket.twr(planet.gravity) < 1) {
                            +""
                            +"<red><bold>The rocket does not have enough thrust to get off the ground"
                        }
                        +""
                        +"<yellow><bold>Click to launch the rocket"
                    }

                    is WorldType.Space -> {
                        name = "Land rocket"

                        val orbiting = worldType.orbiting
                        val cost = orbiting.orbitCost

                        +""
                        if (orbiting is PlanetaryWorld) {
                            var canAerobrake = false
                            if (orbiting.atmosphere.canAerobrake) {
                                +"<green>The atmosphere is thick enough to aerobrake"
                                if (rocket.isFullyShielded) {
                                    +"<green>You can land without using any fuel"
                                    +"<green>(but any heat shielding tiles will be used up)"
                                    canAerobrake = true
                                } else {
                                    +"<red>Your rocket does not have enough shielding to aerobrake"
                                }
                                +""
                            }
                            +"<gold>Delta-v required for landing: $cost m/s"
                            if (rocket.stages.first().deltaV < cost) {
                                +""
                                +"<red><bold>The rocket does not have enough delta-v to land"
                                if (canAerobrake) {
                                    +"<green>However, you can still aerobrake at no fuel cost"
                                }
                            }
                            +""
                            if (canAerobrake) {
                                +"<yellow><bold>Left click to land using engines"
                                +"<yellow><bold>Right click to aerobrake"
                            } else {
                                +"<yellow><bold>Click to land"
                            }
                        } else {
                            +"<red><bold>You cannot land on ${orbiting.name}"
                        }
                    }

                    null -> {
                        name = "Invalid world"
                    }
                }

                if (worldType != null) {
                    onClick { p, action ->
                        val seat = Seat.getSitting(p)
                        if (canLaunch(seat, rocket)) {
                            when (worldType) {
                                is WorldType.Planet -> RocketManager.launchRocket(p, rocket, seat)
                                is WorldType.Space -> {
                                    val orbiting = worldType.orbiting
                                    if (orbiting is PlanetaryWorld) {
                                        RocketManager.landRocket(
                                            p,
                                            rocket,
                                            seat,
                                            action.isRightClicked
                                                    && orbiting.atmosphere.canAerobrake
                                                    && rocket.isFullyShielded
                                        )
                                    } else {
                                        p.sendMessage(NamedTextColor.RED + "You cannot land on ${orbiting.name}")
                                    }
                                }
                            }
                        } else {
                            p.sendMessage(NamedTextColor.RED + "You must be sitting in the captain's chair to operate the rocket")
                        }
                    }
                }
            }
        }.newMenu(itemName, b)
    }
}
