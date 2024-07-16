package io.github.addoncommunity.galactifun.api.objects.properties

import org.bukkit.GameRule
import org.bukkit.World
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

class DayCycle(val duration: Duration) {

    companion object {
        val EARTH_LIKE = DayCycle(1.days)
    }

    private val perFiveSeconds: Long

    init {
        val days = duration.inWholeDays
        val hours = duration.inWholeHours - days * 24
        require(duration.inWholeHours > 1) { "A day cycle must last longer than 1 hour" }
        perFiveSeconds = days * 100L + hours * 4L // magic happens here, do not touch
    }

    fun applyEffects(world: World) {
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
    }

    fun tick(world: World) {
        world.time += perFiveSeconds
    }

    override fun toString(): String {
        val sb = StringBuilder()
        if (duration.inWholeDays > 0) {
            sb.append(duration.inWholeDays)
            sb.append(" day")
            if (duration.inWholeDays > 1) sb.append('s')
        }
        if (duration.inWholeHours > 0) {
            if (sb.isNotEmpty()) sb.append(", ")
            sb.append(duration.inWholeHours % 24)
            sb.append(" hour")
            if (duration.inWholeHours % 24 > 1) sb.append('s')
        }
        return sb.toString()
    }
}