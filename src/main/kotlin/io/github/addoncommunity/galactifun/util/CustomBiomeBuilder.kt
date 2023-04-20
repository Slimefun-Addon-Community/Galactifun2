package io.github.addoncommunity.galactifun.util

import io.github.seggan.custombiomeapi.CustomBiome
import org.bukkit.Color
import org.bukkit.NamespacedKey
import org.bukkit.block.Biome

class CustomBiomeBuilder {
    var isRainy = false
    var skyColor = Color.fromRGB(0x82A7FF) // default temperate sky color
    var fogColor = Color.fromRGB(0xB9D2FF) // default temperate fog color
    var waterColor = Color.fromRGB(0x3F76E4) // default water color
    var waterFogColor = Color.fromRGB(0x050533) // default water fog color
    var foliageColor = Color.fromRGB(0x77AB2F) // plains foliage color
    var grassColor = Color.fromRGB(0x91BD59) // plains grass color
}

inline fun buildCustomBiome(key: NamespacedKey, baseBiome: Biome, builder: CustomBiomeBuilder.() -> Unit): CustomBiome {
    val biomeBuilder = CustomBiomeBuilder()
    biomeBuilder.builder()
    return CustomBiome(
        key,
        baseBiome,
        biomeBuilder.isRainy,
        biomeBuilder.fogColor,
        biomeBuilder.waterColor,
        biomeBuilder.waterFogColor,
        biomeBuilder.skyColor,
        biomeBuilder.foliageColor,
        biomeBuilder.grassColor
    )
}