package io.github.addoncommunity.galactifun

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import org.bukkit.plugin.java.JavaPlugin

@Suppress("UnstableApiUsage")
class Galactifun2Bootstrapper : PluginBootstrap {

    override fun bootstrap(context: BootstrapContext) {
        // Nothing to do here
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return Galactifun2
    }
}