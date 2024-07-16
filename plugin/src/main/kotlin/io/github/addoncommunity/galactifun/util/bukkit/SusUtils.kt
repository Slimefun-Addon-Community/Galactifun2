package io.github.addoncommunity.galactifun.util.bukkit

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.addoncommunity.galactifun.launchAsync
import io.github.addoncommunity.galactifun.pluginInstance
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

suspend fun Player.awaitChatInput(): String {
    return suspendCoroutine { cont ->
        ChatUtils.awaitInput(this) {
            cont.resume(it)
        }
    }
}

suspend fun delayTicks(ticks: Int) {
    delay(ticks.ticks)
}

inline fun <T> Collection<T>.consumeSpreadOut(ticks: Int, crossinline action: suspend (T) -> Unit) {
    val channel = Channel<T>()
    pluginInstance.launchAsync {
        for (item in this@consumeSpreadOut) {
            channel.send(item)
        }
        channel.close()
    }
    val itemsPerTick = size / ticks + 1
    pluginInstance.launch {
        var i = 0
        for (item in channel) {
            action(item)
            if (++i % itemsPerTick == 0) {
                delayTicks(1)
            }
        }
    }
}