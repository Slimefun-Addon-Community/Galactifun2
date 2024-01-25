package io.github.addoncommunity.galactifun.base.objects.earth

import io.github.addoncommunity.galactifun.api.objects.planet.AlienWorld
import io.github.addoncommunity.galactifun.api.objects.planet.gen.WorldGenerator
import io.github.addoncommunity.galactifun.api.objects.properties.DayCycle
import io.github.addoncommunity.galactifun.api.objects.properties.Distance.Companion.kilometers
import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Atmosphere
import io.github.addoncommunity.galactifun.base.BaseUniverse
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class Moon : AlienWorld("Moon", ItemStack(Material.END_STONE)) {
    override val atmosphere = Atmosphere.NONE
    override val dayCycle = DayCycle(29.days + 12.hours)
    override val orbiting = BaseUniverse.earth
    override val orbit = Orbit(382500.kilometers, 27.days)
    override val generator: WorldGenerator = MoonGenerator()
}