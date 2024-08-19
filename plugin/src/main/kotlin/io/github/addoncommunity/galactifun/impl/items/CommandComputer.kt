package io.github.addoncommunity.galactifun.impl.items

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.addoncommunity.galactifun.api.betteritem.BetterSlimefunItem
import io.github.addoncommunity.galactifun.api.betteritem.ItemHandler
import io.github.addoncommunity.galactifun.api.betteritem.Ticker
import io.github.addoncommunity.galactifun.api.rockets.RocketInfo
import io.github.addoncommunity.galactifun.impl.items.abstract.Seat
import io.github.addoncommunity.galactifun.impl.managers.PlanetManager
import io.github.addoncommunity.galactifun.impl.managers.RocketManager
import io.github.addoncommunity.galactifun.pluginInstance
import io.github.addoncommunity.galactifun.units.Acceleration.Companion.metersPerSecondSquared
import io.github.addoncommunity.galactifun.units.Velocity.Companion.metersPerSecond
import io.github.addoncommunity.galactifun.units.abs
import io.github.addoncommunity.galactifun.units.times
import io.github.addoncommunity.galactifun.units.unitSumOf
import io.github.addoncommunity.galactifun.util.*
import io.github.addoncommunity.galactifun.util.bukkit.*
import io.github.seggan.sf4k.location.plus
import io.github.seggan.sf4k.location.position
import io.github.seggan.sf4k.serial.blockstorage.getBlockStorage
import io.github.seggan.sf4k.serial.blockstorage.setBlockStorage
import io.github.seggan.sf4k.serial.pdc.get
import io.github.seggan.sf4k.serial.pdc.set
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import kotlinx.coroutines.future.await
import me.mrCookieSlime.Slimefun.api.BlockStorage
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BoundingBox
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayDeque
import kotlin.jvm.optionals.getOrNull
import kotlin.math.max
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class CommandComputer(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>
) : BetterSlimefunItem(itemGroup, item, recipeType, recipe) {

    private val counters = Object2IntOpenHashMap<BlockPosition>()

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

        p.sendMessage("Enter the destination's x, y, z coordinates separated by commas")
        val coords = p.awaitChatInput()
        if (Seat.getSitting(p) != seat) return
        val match = coordinateRegex.matchEntire(coords)
        if (match == null) {
            p.sendMessage("Invalid coordinates")
            return
        }
        val (x, y, z) = match.destructured
        val dest = space.offset(x.toDouble(), y.toDouble(), z.toDouble())

        val entities = mutableSetOf<Entity>(p)
        rocket.blocks.consumeSpreadOut(200) { b ->
            val block = b.block
            val foundEntities = world.getNearbyEntities(BoundingBox.of(block))
            foundEntities.addAll(
                world.getNearbyEntities(BoundingBox.of(block.getRelative(BlockFace.UP)))
                    .filter { it.isOnGround }
            )
            val serialized = SerializedBlock.serialize(block)
            val display = serialized.createDisplayEntity(b.location)
            entities.add(display)
            entities.addAll(foundEntities.filter { it.vehicle == null }.flatMap(::freezeEntity))
        }

        // Random launch messages
        val launched = AtomicBoolean(false)
        pluginInstance.launch {
            val launchMessages = ArrayDeque(pluginInstance.launchMessages)
            launchMessages.shuffle()
            while (true) {
                delayTicks(Random.nextInt(15, 30))
                if (launched.get()) break
                for (player in entities.filterIsInstance<Player>()) {
                    player.sendMessage(launchMessages.removeFirst() + "...")
                }
            }
        }

        // Engine smoke
        pluginInstance.launch {
            while (!launched.get()) {
                for ((_, engine) in firstStage.engines) {
                    world.spawnParticle(
                        Particle.CAMPFIRE_SIGNAL_SMOKE,
                        engine.location.add(0.0, -1.0, 0.0),
                        Random.nextInt(1, 3),
                        0.5,
                        0.0,
                        0.5
                    )
                }
                delayTicks(2)
            }
        }

        // Countdown
        repeat(10) {
            for (player in entities.filterIsInstance<Player>()) {
                player.sendMessage(NamedTextColor.GOLD + "Launching in ${10 - it}...")
            }
            delayTicks(20)
        }
        launched.set(true)

        val offsets = entities.associateWith { it.location.subtract(pos.toLocation()) }
        val maxHeight = world.maxHeight
        var position = pos.y.toDouble()

        val weight = rocket.wetMass * currentPlanet.gravity
        val netForce = firstStage.engines.unitSumOf { it.first.thrust } - weight
        val acceleration = 0.5.metersPerSecondSquared
        val marginalAcceleration = acceleration * 0.05.seconds
        var speed = 0.0.metersPerSecond
        while (position < maxHeight) {
            for (entity in entities) {
                entity.galactifunTeleport(
                    entity.location.add(0.0, (speed / 20).metersPerSecond, 0.0)
                ).await()
                position = max(position, entity.location.y)
            }
            speed += marginalAcceleration
            delayTicks(1)
        }

        for (entity in entities) {
            entity.galactifunTeleport(
                dest + offsets[entity]!!.copy(world = PlanetManager.spaceWorld)
            ).await()
            unfreezeEntity(entity)
        }
    }
}

private val coordinateRegex = """\s*(-?\d+)\s*,\s*(-?\d+)\s*,\s*(-?\d+)\s*""".toRegex()
private val GRAVITY_KEY = "has_gravity".key()
private val AI_KEY = "has_ai".key()
private val FLIGHT_KEY = "is_flight".key()

fun freezeEntity(entity: Entity): List<Entity> {
    val pdc = entity.persistentDataContainer
    pdc.set(GRAVITY_KEY, entity.hasGravity())
    entity.setGravity(false)
    if (entity is LivingEntity) {
        if (entity is Player) {
            pdc.set(FLIGHT_KEY, entity.allowFlight)
            entity.allowFlight = false
        } else {
            pdc.set(AI_KEY, entity.hasAI())
            entity.setAI(false)
        }
    }
    return entity.passengers.flatMap(::freezeEntity) + entity
}

fun unfreezeEntity(entity: Entity) {
    val pdc = entity.persistentDataContainer
    when (entity) {
        is Player -> {
            entity.sendMessage("You have arrived at your destination")
            entity.allowFlight = pdc.get(FLIGHT_KEY) ?: false
        }

        is BlockDisplay -> SerializedBlock.loadFromDisplayEntity(entity, true)
            ?.place(entity.location)

        else -> {
            entity.setGravity(pdc.get(GRAVITY_KEY) ?: true)
            if (entity is LivingEntity) {
                entity.setAI(pdc.get(AI_KEY) ?: true)
            }
        }
    }
}