package io.github.addoncommunity.galactifun.util.bukkit

import io.github.addoncommunity.galactifun.pluginInstance
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
import java.util.concurrent.CompletableFuture

internal fun String.key(): NamespacedKey = NamespacedKey(pluginInstance, this)

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
fun Entity.galactifunTeleport(
    dest: Location,
    reason: PlayerTeleportEvent.TeleportCause = PlayerTeleportEvent.TeleportCause.PLUGIN
): CompletableFuture<Boolean> {
    setMetadata("galactifun.teleporting", DummyMetadataValue)
    return teleportAsync(
        dest,
        reason
    ).thenApply {
        removeMetadata("galactifun.teleporting", pluginInstance)
        it
    }
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