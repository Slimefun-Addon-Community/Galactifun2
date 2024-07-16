package io.github.addoncommunity.galactifun.util.bukkit

import io.github.addoncommunity.galactifun.pluginInstance
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.*
import org.bukkit.entity.Entity
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
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
    preservePassengers: Boolean = true,
    reason: PlayerTeleportEvent.TeleportCause = PlayerTeleportEvent.TeleportCause.PLUGIN
): CompletableFuture<Boolean> {
    if (preservePassengers && passengers.isNotEmpty()) {
        val futures = mutableListOf<CompletableFuture<Boolean>>()
        val passengers = this.passengers
        for (passenger in passengers) {
            removePassenger(passenger)
            futures += passenger.galactifunTeleport(dest, true, reason)
        }
        var future = galactifunTeleport(dest, false, reason)
        for (passengerFuture in futures) {
            future = future.thenCombine(passengerFuture) { a, b -> a && b }
        }
        return future.thenApply {
            if (it) {
                passengers.forEach(::addPassenger)
            }
            it
        }
    } else {
        setMetadata("galactifun.teleporting", DummyMetadataValue)
        return teleportAsync(dest, reason).thenApply {
            removeMetadata("galactifun.teleporting", pluginInstance)
            it
        }
    }
}

operator fun TextColor.plus(s: String): TextComponent = Component.text()
    .color(this)
    .decorations(EnumSet.allOf(TextDecoration::class.java), false)
    .content(s)
    .build()

inline fun ItemStack.modifyLore(modifier: (MutableList<Component>) -> Unit) {
    val meta = itemMeta ?: Bukkit.getItemFactory().getItemMeta(type)
    val lore = meta.lore() ?: mutableListOf()
    modifier(lore)
    meta.lore(lore)
    itemMeta = meta
}

operator fun <T : Keyed> Tag<T>.contains(item: T): Boolean = isTagged(item)

fun String.miniMessageToLegacy(): String = LegacyComponentSerializer.legacyAmpersand()
    .serialize(MiniMessage.miniMessage().deserialize(this))

fun locationZero(world: World?): Location = Location(world, 0.0, 0.0, 0.0)