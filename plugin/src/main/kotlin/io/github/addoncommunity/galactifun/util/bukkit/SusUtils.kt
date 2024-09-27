package io.github.addoncommunity.galactifun.util.bukkit

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.addoncommunity.galactifun.Galactifun2
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.entity.Player
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

inline fun <T> Collection<T>.consumeSpreadOut(ticks: Int, crossinline action: suspend (T) -> Unit): Job {
    val itemsPerTick = size / ticks + 1
    return Galactifun2.launch {
        for ((i, item) in this@consumeSpreadOut.withIndex()) {
            action(item)
            if (i % itemsPerTick == 0) {
                delayTicks(1)
            }
        }
    }
}