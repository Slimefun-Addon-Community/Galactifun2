package io.github.addoncommunity.galactifun.util.bukkit

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.addoncommunity.galactifun.Galactifun2
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.Entity
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.structure.Structure
import org.bukkit.util.Vector
import java.util.*

internal fun String.key(): NamespacedKey = NamespacedKey(Galactifun2, this)

fun Location.copy(
    world: World? = this.world,
    x: Double = this.x,
    y: Double = this.y,
    z: Double = this.z,
    yaw: Float = this.yaw,
    pitch: Float = this.pitch
): Location = Location(world, x, y, z, yaw, pitch)

fun Vector.copy(x: Double = this.x, y: Double = this.y, z: Double = this.z): Vector = Vector(x, y, z)

inline fun <reified T : Entity> World.nearbyEntitiesByType(
    location: Location,
    radius: Double,
    crossinline predicate: (T) -> Boolean = { true }
): List<T> = buildList {
    for (entity in getNearbyEntities(location, radius, radius, radius)) {
        if (entity is T && predicate(entity)) {
            add(entity)
        }
    }
}

inline fun <reified T : Entity> World.summon(location: Location): T = spawn(location, T::class.java)

operator fun RegionAccessor.get(x: Int, y: Int, z: Int): Material = getType(x, y, z)
operator fun RegionAccessor.get(location: Location): Material = getType(location)

operator fun RegionAccessor.set(x: Int, y: Int, z: Int, material: Material) = setType(x, y, z, material)
operator fun RegionAccessor.set(location: Location, material: Material) = setType(location, material)

/**
 * Teleports while telling Galactifun that the teleport should not be blocked.
 * Can be used to teleport entities with passengers.
 */
suspend fun Entity.teleportSpecialAsync(
    dest: Location,
    reason: PlayerTeleportEvent.TeleportCause = PlayerTeleportEvent.TeleportCause.PLUGIN
): Boolean = coroutineScope {
    setMetadata("galactifun.teleporting", DummyMetadataValue)
    val tp = mutableListOf<Deferred<Pair<Entity, Boolean>>>()
    for (passenger in passengers) {
        removePassenger(passenger)
        tp += async { passenger to passenger.teleportSpecialAsync(dest, reason) }
    }
    val (entities, results) = tp.awaitAll().unzip()
    if (results.all { it }) {
        val result = teleportAsync(
            dest,
            reason
        ).await()
        removeMetadata("galactifun.teleporting", Galactifun2)
        for (entity in entities) {
            addPassenger(entity)
        }
        result
    } else {
        false
    }
}

fun Entity.teleportSpecial(
    dest: Location,
    reason: PlayerTeleportEvent.TeleportCause = PlayerTeleportEvent.TeleportCause.PLUGIN
) {
    Galactifun2.launch { teleportSpecialAsync(dest, reason) }
}

inline fun ItemStack.modifyLore(modifier: (MutableList<Component>) -> Unit) {
    val meta = itemMeta ?: Bukkit.getItemFactory().getItemMeta(type)
    val lore = meta.lore() ?: mutableListOf()
    modifier(lore)
    meta.lore(lore)
    itemMeta = meta
}

operator fun <T : Keyed> Tag<T>.contains(item: T): Boolean = isTagged(item)

fun Structure.placeDefault(
    location: Location,
    includeEntities: Boolean = true,
    rotation: StructureRotation = StructureRotation.NONE,
    mirror: Mirror = Mirror.NONE,
    palette: Int = 0,
    integrity: Float = 1.0f,
    random: Random = Random(location.world.seed)
) = place(location, includeEntities, rotation, mirror, palette, integrity, random)