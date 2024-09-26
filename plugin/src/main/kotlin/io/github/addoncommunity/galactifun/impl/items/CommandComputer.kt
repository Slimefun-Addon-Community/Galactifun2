package io.github.addoncommunity.galactifun.impl.items

import com.destroystokyo.paper.ParticleBuilder
import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.addoncommunity.galactifun.api.betteritem.BetterSlimefunItem
import io.github.addoncommunity.galactifun.api.betteritem.ItemHandler
import io.github.addoncommunity.galactifun.api.betteritem.Ticker
import io.github.addoncommunity.galactifun.api.rockets.RocketInfo
import io.github.addoncommunity.galactifun.impl.items.abstract.Seat
import io.github.addoncommunity.galactifun.impl.managers.PlanetManager
import io.github.addoncommunity.galactifun.impl.managers.RocketManager
import io.github.addoncommunity.galactifun.pluginInstance
import io.github.addoncommunity.galactifun.units.abs
import io.github.addoncommunity.galactifun.util.*
import io.github.addoncommunity.galactifun.util.bukkit.*
import io.github.seggan.sf4k.extensions.minus
import io.github.seggan.sf4k.extensions.plus
import io.github.seggan.sf4k.extensions.position
import io.github.seggan.sf4k.serial.blockstorage.getBlockStorage
import io.github.seggan.sf4k.serial.blockstorage.setBlockStorage
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import io.papermc.paper.event.entity.EntityMoveEvent
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import me.mrCookieSlime.Slimefun.api.BlockStorage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import net.kyori.adventure.util.Ticks
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BoundingBox
import java.util.*
import kotlin.collections.ArrayDeque
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

        private val frozenEntities = mutableSetOf<Entity>()

        init {
            Bukkit.getPluginManager().registerEvents(this, pluginInstance)
        }

        @EventHandler
        private fun onPlayerMove(e: PlayerMoveEvent) {
            val player = e.player
            if (player in frozenEntities && e.hasChangedPosition()) {
                e.isCancelled = true
            }
        }

        @EventHandler
        private fun onEntityMove(e: EntityMoveEvent) {
            val entity = e.entity
            if (entity in frozenEntities && e.hasChangedPosition()) {
                e.isCancelled = true
            }
        }

        @EventHandler
        private fun onPlayerLeave(e: PlayerQuitEvent) {
            frozenEntities.remove(e.player)
        }
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
        val pos = e.clickedBlock.getOrNull()?.position ?: return
        rescanRocket(pos)
        val info = RocketManager.getInfo(pos)!!
        if (info.blocks.isEmpty()) {
            p.sendMessage(NamedTextColor.RED + "No rocket detected")
            return
        }
        val seat = Seat.getSitting(p)
        if (seat != null
            && BlockStorage.check(seat) is CaptainsChair
            && seat.getBlockStorage<BlockPosition>("rocket") == pos
        ) {
            RocketManager.launches += pluginInstance.launch { launchRocket(p, pos, info, seat) }
        } else {
            e.player.sendMessage(NamedTextColor.GOLD + info.info)
        }
    }

    private suspend fun launchRocket(p: Player, pos: BlockPosition, rocket: RocketInfo, seat: Location) {
        val world = p.world
        val currentPlanet = PlanetManager.getByWorld(world)
        if (currentPlanet == null) {
            p.sendMessage(NamedTextColor.RED + "You are not on a planet")
            return
        }
        val space = currentPlanet.orbitPosition
        p.sendMessage("The rocket's current position is ${pos.x}, ${pos.y}, ${pos.z}")
        p.sendMessage("The cost to travel to space is %.2s".format(currentPlanet.surfaceToOrbitCost))

        val firstStage = rocket.stages.first()
        val dVMinusSpace = firstStage.deltaV - currentPlanet.surfaceToOrbitCost
        if (dVMinusSpace.metersPerSecond <= 0) {
            p.sendMessage(
                NamedTextColor.RED +
                        "The rocket needs %.2s more delta-v to reach space".format(abs(dVMinusSpace))
            )
            return
        }
        p.sendMessage("The rocket will have %.2s delta-v left after reaching space".format(dVMinusSpace))

        if (rocket.twr(currentPlanet.gravity) < 1) {
            p.sendMessage("The rocket doesn't have enough thrust-to-weight ratio to get off the ground")
            return
        }

        val hasBlocking = rocket.blocks.groupBy { it.x to it.z }
            .map { (_, blocks) -> blocks.maxBy { it.y } }
            .any { !it.isHighest() }

        if (hasBlocking) {
            p.sendMessage("The rocket is blocked by terrain")
            return
        }

        p.sendMessage("Enter the destination's x y z coordinates separated by spaces")
        val coords = p.awaitChatInput()
        if (Seat.getSitting(p) != seat) return
        val match = coordinateRegex.matchEntire(coords)
        if (match == null) {
            p.sendMessage("Invalid coordinates")
            return
        }
        val (x, y, z) = match.destructured
        val dest = space.offset(x.toDouble(), y.toDouble(), z.toDouble())

        val entities = mutableSetOf<Entity>()
        val players = mutableSetOf<Player>()
        val blocks = rocket.blocks + rocket.blocks.map { it.getFace(BlockFace.UP) }
        blocks.toSet().consumeSpreadOut(200) { b ->
            val block = b.block
            for (entity in world.getNearbyEntities(BoundingBox.of(block))) {
                if (entity is Player) {
                    players.add(entity)
                    entities.add(entity)
                } else {
                    entity.remove()
                }
            }
        }

        var launched = false

        // Engine smoke
        pluginInstance.launch {
            while (!launched) {
                for ((_, engine) in firstStage.engines) {
                    ParticleBuilder(Particle.CAMPFIRE_SIGNAL_SMOKE)
                        .location(engine.location)
                        .offset(0.5, 0.5, 0.5)
                        .count(1)
                        .spawn()
                }
                delayTicks(2)
            }
        }

        // Countdown
        val launchMessages = ArrayDeque(pluginInstance.launchMessages)
        launchMessages.shuffle()
        repeat(10) {
            val message = NamedTextColor.GOLD + "${launchMessages.removeFirst()}..."
            for (player in players) {
                player.sendMessage(message)
                player.showTitle(
                    Title.title(
                        "<green><bold>T-${10 - it}".miniComponent(),
                        Component.empty(),
                        Title.Times.times(
                            Ticks.duration(5),
                            Ticks.duration(10),
                            Ticks.duration(5)
                        )
                    )
                )
            }
            delayTicks(20)
        }
        launched = true

        val blockOffsets = blocks.associateWith { it.location - pos.toLocation() }
        val entityOffsets = entities.associateWith { it.location - pos.toLocation() }
        val destMinHeight = PlanetManager.spaceWorld.minHeight
        val globalOffset = (destMinHeight - blockOffsets.minOf { (_, offset) -> offset.y })
            .coerceAtLeast(0.0)

        val explosionLocations = mutableSetOf<Location>()
        for (blockPos in blocks) {
            val oldBlock = blockPos.block
            val newLoc = dest + blockOffsets[blockPos]!!.copy(world = PlanetManager.spaceWorld)
            newLoc.y += globalOffset
            val newBlock = newLoc.block
            if (!newBlock.type.isAir && !newBlock.isReplaceable) {
                explosionLocations.add(newLoc)
            }
            newBlock.type = oldBlock.type
            newBlock.blockData = oldBlock.blockData
            @Suppress("UnstableApiUsage")
            oldBlock.state.copy(newLoc)

            if (BlockStorage.hasBlockInfo(oldBlock)) {
                val oldBs = BlockStorage.getBlockInfoAsJson(oldBlock)
                BlockStorage.clearBlockInfo(oldBlock)
                BlockStorage.setBlockInfo(newBlock, oldBs, true)
            }

            oldBlock.type = Material.AIR
        }

        for (entity in entities) {
            val end = dest + entityOffsets[entity]!!.copy(world = PlanetManager.spaceWorld)
            end.y += globalOffset
            entity.galactifunTeleport(end)
        }

        delayTicks(1)

        for (loc in explosionLocations) {
            loc.world.createExplosion(loc, 3f, false, true)
        }

        /*
        TODO make rockets move

        val maxHeight = world.maxHeight
        var position = pos.y.toDouble()
        val offsets = entities.associateWith { it.location - pos.toLocation() }

        val weight = rocket.wetMass * currentPlanet.gravity
        val netForce = firstStage.engines.unitSumOf { it.first.thrust } - weight
        val acceleration = netForce divToAcceleration rocket.wetMass
        val marginalAcceleration = acceleration * 0.05.seconds
        val marginalAccelerationVector = UnitVector.Y * marginalAcceleration.metersPerSecond
        while (position < maxHeight) {
            for (entity in entities) {
                entity.velocity += marginalAccelerationVector
                position = max(position, entity.location.y)
            }
            delayTicks(1)
        }

        for (entity in entities) {
            pluginInstance.launch {
                if (!entity.galactifunTeleport(
                    dest + offsets[entity]!!.copy(world = PlanetManager.spaceWorld)
                ).await()) {
                    error("Failed to teleport entity")
                }
                delayTicks(1)
                when (entity) {
                    is FallingBlock -> entity.toBlock()
                    is Player -> frozenPlayers.remove(entity)
                }
            }
        }
         */
    }
}

private val coordinateRegex = """\s*(-?\d+)\s+(-?\d+)\s+(-?\d+)\s*""".toRegex()