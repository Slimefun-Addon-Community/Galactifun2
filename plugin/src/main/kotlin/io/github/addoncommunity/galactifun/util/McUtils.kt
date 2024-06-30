package io.github.addoncommunity.galactifun.util

import io.github.addoncommunity.galactifun.pluginInstance
import kotlinx.coroutines.suspendCancellableCoroutine
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.*
import org.bukkit.entity.Entity
import org.bukkit.event.*
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.resume

fun String.key(): NamespacedKey = NamespacedKey(pluginInstance, this)

fun Location.withWorld(world: World): Location = Location(world, x, y, z, yaw, pitch)

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
 * Teleports while telling Galactifun that the teleport should not be blocked
 */
fun Entity.galactifunTeleport(
    dest: Location,
    reason: PlayerTeleportEvent.TeleportCause = PlayerTeleportEvent.TeleportCause.PLUGIN
): CompletableFuture<Boolean> {
    setMetadata("galactifun.teleporting", DummyMetadataValue)
    return teleportAsync(dest, reason).thenApply {
        removeMetadata("galactifun.teleporting", pluginInstance)
        it
    }
}

operator fun TextColor.plus(s: String): TextComponent = Component.text()
    .color(this)
    .decorations(EnumSet.allOf(TextDecoration::class.java), false)
    .content(s)
    .build()

operator fun <T : Keyed> Tag<T>.contains(item: T): Boolean = isTagged(item)

fun String.miniMessageToLegacy(): String = LegacyComponentSerializer.legacyAmpersand()
    .serialize(MiniMessage.miniMessage().deserialize(this))

fun locationZero(world: World?): Location = Location(world, 0.0, 0.0, 0.0)

suspend inline fun <reified E : Event> waitForEvent(
    priority: EventPriority = EventPriority.NORMAL,
    cancelIfEventCancelled: Boolean = false
): E {
    return suspendCancellableCoroutine { cont ->
        Bukkit.getPluginManager().registerEvent(
            E::class.java,
            object : Listener {},
            priority,
            { listener, event ->
                HandlerList.unregisterAll(listener)
                if (cancelIfEventCancelled && event is Cancellable && event.isCancelled) {
                    cont.cancel()
                } else {
                    cont.resume(event as E)
                }
            },
            pluginInstance
        )
    }
}
