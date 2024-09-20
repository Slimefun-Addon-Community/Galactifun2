package io.github.addoncommunity.galactifun.util.bukkit

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

fun String.miniMessageToLegacy(): String = LegacyComponentSerializer.legacyAmpersand()
    .serialize(MiniMessage.miniMessage().deserialize(this))

fun String.miniComponent(): Component =
    Component.text()
        .decoration(TextDecoration.ITALIC, false)
        .append(MiniMessage.miniMessage().deserialize(this))
        .build()

fun String.legacyDefaultColor(color: Char): String {
    if (startsWith('&') || isBlank()) return this
    return "&$color$this"
}