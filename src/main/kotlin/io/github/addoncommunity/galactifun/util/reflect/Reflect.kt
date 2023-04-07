package io.github.addoncommunity.galactifun.util.reflect

import org.bukkit.Bukkit

object Reflect {

    @Suppress("DEPRECATION")
    val version = Bukkit.getUnsafe()::class.getFunction("getMappingsVersion").call(Bukkit.getUnsafe()) as String
}