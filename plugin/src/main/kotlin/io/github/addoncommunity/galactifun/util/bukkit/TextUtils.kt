package io.github.addoncommunity.galactifun.util.bukkit

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import java.util.*

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

operator fun TextColor.plus(s: String): TextComponent = Component.text()
    .color(this)
    .decorations(EnumSet.allOf(TextDecoration::class.java), false)
    .content(s)
    .build()