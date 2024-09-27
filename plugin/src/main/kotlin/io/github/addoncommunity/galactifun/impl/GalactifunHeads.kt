package io.github.addoncommunity.galactifun.impl

import io.github.addoncommunity.galactifun.util.items.MaterialType
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils

enum class GalactifunHeads(private val texture: String) {

    // https://minecraft-heads.com/custom-heads/head/81364-floppy-disc-icon
    FLOPPY_DISK("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTYyOWM4MTNlNTAxZjI0YmIyZjRjYTAyMWVlNTg2MmU0ZTIxNTgxODdmMzA3MjcyZWI2NWExZTY0ZDMwOTE0OCJ9fX0="),

    // https://minecraft-heads.com/custom-heads/head/57245-file-explorer
    FILE_EXPLORER("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzczZThiZDNjNDNjNDUxNGM3NjQ4MWNhMWRhZjU1MTQ5ZGZjOTNiZDFiY2ZhOGFiOTQzN2I5ZjdlYjMzOTJkOSJ9fX0="),

    // https://minecraft-heads.com/custom-heads/head/94698-forest-green-l
    LETTER_L("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmZhMDI5NjgyMjhkOWM0MDExZjAxMWRiNzAxZjJjNzUwZTRlYTZhZTJkM2E2MDM1NWJkZTdlNjgyMGVjZThmNyJ9fX0="),

    // https://minecraft-heads.com/custom-heads/head/94709-forest-green-w
    LETTER_W("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTZiZDJkZGMzZTFmYTdiY2I4YjZiYjViYTRmMGFlZWI4MjgzNDFlYWUwYTRkNzVmYzk4NWUzNzE5MWJkMGY1NCJ9fX0="),

    // https://minecraft-heads.com/custom-heads/head/94694-forest-green-h
    LETTER_H("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWRiMDJiMDQwYzM3MDE1ODkyYTNhNDNkM2IxYmZkYjJlMDFhMDJlZGNjMmY1YjgyMjUwZGNlYmYzZmY0ZjAxZSJ9fX0="),

    // https://minecraft-heads.com/custom-heads/head/94710-forest-green-x
    LETTER_X("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDZhZTIxMWI0NDAzYjg5NjNjNjc0NWYzYzk4ZWJlNzNhMmI0ZTk3YzQwYTc4YjJmZDQwM2EwOWMwZmNhZDZkIn19fQ=="),

    // https://minecraft-heads.com/custom-heads/head/94711-forest-green-y
    LETTER_Y("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTE5N2MwNjFjM2E1M2RmYTQ1ODMzZDgwYzM3Y2U4ZDEyYTVjMzZhMTViYmRlMjQ4OWZjYjFjYjMzYTJhZGZmOCJ9fX0="),

    // https://minecraft-heads.com/custom-heads/head/94712-forest-green-z
    LETTER_Z("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmEwOTg5NzVhNzI3ZTkyYmQ5YmM5OGIxY2JmMTQwZTdhNDFhOTkyZmU2NGNmYmI3MTk2ZTdkYmRhNDM0OTczIn19fQ=="),

    // https://minecraft-heads.com/custom-heads/head/46571-rocket
    ROCKET("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTRmOWRkMzkwNzAxODY0MWJkYjEwNWI5NjQ3YzA4MjI3OTYyZjEwNjUwMmE2ODI5YWRkNGI5MDhmNzQyYTgzNyJ9fX0="),

    // https://minecraft-heads.com/custom-heads/head/97906-info
    INFO("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2ZmOGNmOGVlMmYyMzNiMTcyZTE2MjE4MmNlZmFjODRiMWI4ZTEzZDk0OGMxNWRjODkyNzMxYmIyYWJhNzI5ZCJ9fX0="),
    ;

    val item = SlimefunUtils.getCustomHead(texture)
    val materialType: MaterialType = MaterialType.Head(texture)
}