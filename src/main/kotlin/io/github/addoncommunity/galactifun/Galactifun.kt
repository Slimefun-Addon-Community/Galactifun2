package io.github.addoncommunity.galactifun

import io.github.addoncommunity.galactifun.base.BaseUniverse
import io.github.addoncommunity.galactifun.util.reflect.Reflect
import io.github.thebusybiscuit.slimefun4.api.MinecraftVersion
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class Galactifun : AbstractAddon() {

    companion object;

    override fun onLoad() {
        Bukkit.spigot().config["world-settings.default.verbose"] = false
    }

    override fun onEnable() {
        pluginInstance = this

        var shouldDisable = false
        if (!PaperLib.isPaper()) {
            log(Level.SEVERE, "Galactifun only supports Paper and its forks (e.x. Airplane and Purpur)")
            log(Level.SEVERE, "Please use Paper or a fork of Paper")
            shouldDisable = true
        }
        if (Slimefun.getMinecraftVersion().isBefore(MinecraftVersion.MINECRAFT_1_19)) {
            log(Level.SEVERE, "Galactifun only supports Minecraft 1.19 and above")
            log(Level.SEVERE, "Please use Minecraft 1.19 or above")
            shouldDisable = true
        }
        if (Bukkit.getPluginManager().isPluginEnabled("ClayTech")) {
            log(Level.SEVERE, "Galactifun will not work properly with ClayTech")
            log(Level.SEVERE, "Please disable ClayTech")
            shouldDisable = true
        }

        if (shouldDisable) {
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        Metrics(this, 11613)

        BaseUniverse.init()

        log(Reflect.version)

        runTask {
            log(
                Level.INFO,
                "################# Galactifun2 $pluginVersion #################",
                "",
                "Galactifun2 is open source, you can contribute or report bugs at $bugTrackerURL",
                "Join the Slimefun Addon Community Discord: discord.gg/SqD3gg5SAU",
                "",
                "###################################################"
            )
        }
    }

    override fun getJavaPlugin(): JavaPlugin = this

    override fun getBugTrackerURL(): String = "https://github.com/Slimefun-Addon-Community/Galactifun2/issues"
}

lateinit var pluginInstance: Galactifun
    private set

fun Galactifun.log(level: Level, vararg messages: String) {
    for (message in messages) {
        logger.log(level, message)
    }
}

fun Galactifun.log(vararg messages: String) = log(Level.INFO, *messages)

fun Galactifun.runTask(runnable: Runnable) {
    server.scheduler.runTask(this, runnable)
}

fun Galactifun.runTaskRepeat(period: Long, delay: Long = 0, runnable: Runnable) {
    server.scheduler.runTaskTimer(this, runnable, delay, period)
}