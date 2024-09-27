package io.github.addoncommunity.galactifun.impl.managers

import com.destroystokyo.paper.ParticleBuilder
import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.addoncommunity.galactifun.Galactifun2
import io.github.addoncommunity.galactifun.api.blocks.ReentryBurnable
import io.github.addoncommunity.galactifun.api.objects.planet.PlanetaryWorld
import io.github.addoncommunity.galactifun.api.rockets.RocketInfo
import io.github.addoncommunity.galactifun.impl.items.abstract.Seat
import io.github.addoncommunity.galactifun.units.abs
import io.github.addoncommunity.galactifun.util.bukkit.*
import io.github.seggan.sf4k.extensions.minus
import io.github.seggan.sf4k.extensions.plus
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import kotlinx.coroutines.Job
import me.mrCookieSlime.Slimefun.api.BlockStorage
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import net.kyori.adventure.util.Ticks
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox

@Suppress("DuplicatedCode")
object RocketManager {

    const val BLOCKS_PER_SCAN = 64

    private val rockets = mutableMapOf<BlockPosition, RocketInfo>()

    fun register(rocket: RocketInfo) {
        rockets[rocket.commandComputer] = rocket
    }

    fun unregister(rocket: RocketInfo) {
        rockets.remove(rocket.commandComputer)
    }

    fun getInfo(commandComputer: BlockPosition) = rockets[commandComputer]

    fun launchRocket(p: Player, rocket: RocketInfo, seat: Location) {
        Galactifun2.launch {
            val pos = rocket.commandComputer
            val world = p.world
            val currentPlanet = PlanetManager.getByWorld(world) ?: return@launch
            val firstStage = rocket.stages.first()
            val dVMinusSpace = firstStage.deltaV - currentPlanet.orbitCost
            if (dVMinusSpace.metersPerSecond <= 0) {
                p.sendMessage(
                    NamedTextColor.RED +
                            "The rocket needs %.2s more delta-v to reach space".format(abs(dVMinusSpace))
                )
                return@launch
            }
            p.sendMessage("The rocket will have %.2s delta-v left after reaching space".format(dVMinusSpace))

            if (rocket.twr(currentPlanet.gravity) < 1) {
                p.sendMessage("The rocket doesn't have enough thrust-to-weight ratio to get off the ground")
                return@launch
            }

            val hasBlocking = rocket.blocks.groupBy { it.x to it.z }
                .map { (_, blocks) -> blocks.maxBy { it.y } }
                .any { !it.isHighest() }

            if (hasBlocking) {
                p.sendMessage("The rocket is blocked by terrain")
                return@launch
            }

            p.sendMessage("Enter the destination's x y z coordinates separated by spaces")
            val coords = p.awaitChatInput()
            if (Seat.getSitting(p) != seat) return@launch
            val match = coordinateRegex.matchEntire(coords)
            if (match == null) {
                p.sendMessage("Invalid coordinates")
                return@launch
            }
            val (x, y, z) = match.destructured
            val dest = currentPlanet.orbitPosition.offset(x.toDouble(), y.toDouble(), z.toDouble())

            val entities = mutableSetOf<Entity>()
            val players = mutableSetOf<Player>()
            startEntityScan(200, rocket.blocks, entities, players)

            var launched = false

            // Engine smoke
            Galactifun2.launch {
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
            val launchMessages = ArrayDeque(Galactifun2.launchMessages)
            launchMessages.shuffle()
            repeat(10) {
                val countdown = "<green><bold>T-${10 - it}".miniComponent()
                val message = "<white>${launchMessages.removeFirst()}...".miniComponent()
                for (player in players) {
                    player.showTitle(
                        Title.title(
                            countdown,
                            message,
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

            val destMinHeight = PlanetManager.spaceWorld.minHeight
            val offset = (destMinHeight - (rocket.blocks.minOf { it.y } - pos.y)).coerceAtLeast(0)
            moveRocket(pos.location, dest, offset, rocket.blocks, entities, false)

            // TODO make rockets move
        }
    }

    fun landRocket(p: Player, rocket: RocketInfo, seat: Location, aerobrake: Boolean) {
        Galactifun2.launch {
            val pos = rocket.commandComputer
            val destPlanet = PlanetManager.getOrbiting(pos.location) as? PlanetaryWorld ?: return@launch
            if (!aerobrake) {
                val firstStage = rocket.stages.first()
                val cost = destPlanet.orbitCost
                if (firstStage.deltaV < cost) {
                    p.sendMessage(NamedTextColor.RED + "The rocket does not have enough fuel to land")
                    return@launch
                }
            }
            p.sendMessage("Enter the destination's x z coordinates separated by spaces")
            val coords = p.awaitChatInput()
            if (Seat.getSitting(p) != seat) return@launch
            val match = xzRegex.matchEntire(coords)
            if (match == null) {
                p.sendMessage("Invalid coordinates")
                return@launch
            }
            val (x, z) = match.destructured

            val entities = mutableSetOf<Entity>()
            val players = mutableSetOf<Player>()
            startEntityScan(0, rocket.blocks, entities, players).join()

            val dest = destPlanet.world.getHighestBlockAt(x.toInt(), z.toInt()).location
            val offset = (dest.blockY - (rocket.blocks.minOf { it.y } - pos.y)).coerceAtLeast(1)
            moveRocket(pos.location, dest, offset, rocket.blocks, entities, aerobrake)
        }
    }
}

private val coordinateRegex = """\s*(-?\d+)\s+(-?\d+)\s+(-?\d+)\s*""".toRegex()
private val xzRegex = """\s*(-?\d+)\s+(-?\d+)\s*""".toRegex()

private suspend fun moveRocket(
    src: Location,
    dest: Location,
    yOffset: Int,
    blocks: Set<BlockPosition>,
    entities: Set<Entity>,
    burn: Boolean
) {
    val blockOffsets = blocks.associateWith { it.location - src }
    val entityOffsets = entities.associateWith { it.location - src }

    var copied = 0
    val explosionLocations = mutableSetOf<Location>()
    for (blockPos in blocks) {
        val oldBlock = blockPos.block
        val newLoc = dest + blockOffsets[blockPos]!!.copy(world = PlanetManager.spaceWorld)
        newLoc.y += yOffset
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
            BlockStorage.setBlockInfo(newBlock, oldBs, true)
            val type = BlockStorage.check(oldBlock)
            if (burn && type is ReentryBurnable) {
                type.convert(newBlock)
            }
            BlockStorage.clearBlockInfo(oldBlock)
        }

        oldBlock.type = Material.AIR

        if (copied++ >= 64) {
            delayTicks(1)
            copied = 0
        }
    }

    for (entity in entities) {
        val end = dest + entityOffsets[entity]!!.copy(world = PlanetManager.spaceWorld)
        end.y += yOffset
        entity.teleportSpecial(end)
    }

    delayTicks(1)
    for (loc in explosionLocations) {
        loc.world.createExplosion(loc, 3f, false, true)
    }
}

private fun startEntityScan(
    ticks: Int,
    blocks: Set<BlockPosition>,
    entities: MutableSet<Entity>,
    players: MutableSet<Player>
): Job {
    val scan = blocks + blocks.map { it.getFace(BlockFace.UP) }
    return scan.consumeSpreadOut(ticks) { b ->
        val block = b.block
        for (entity in b.world.getNearbyEntities(BoundingBox.of(block))) {
            if (entity is Player) {
                players.add(entity)
            }
            entities.add(entity)
        }
    }
}