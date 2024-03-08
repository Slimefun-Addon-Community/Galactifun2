package io.github.addoncommunity.galactifun.util.items

import io.github.addoncommunity.galactifun.units.Mass
import io.github.addoncommunity.galactifun.units.Mass.Companion.kilograms
import io.github.addoncommunity.galactifun.units.Mass.Companion.tons
import io.github.addoncommunity.galactifun.util.contains
import io.github.thebusybiscuit.slimefun4.utils.tags.SlimefunTag
import org.bukkit.Material
import org.bukkit.block.Block

private val INFINITE_MASS = Double.POSITIVE_INFINITY.kilograms

val Block.mass: Mass
    get() = when {
        type == Material.AIR || type == Material.CAVE_AIR || type == Material.VOID_AIR -> Mass.ZERO
        type in SlimefunTag.UNBREAKABLE_MATERIALS -> INFINITE_MASS
        type in SlimefunTag.FLUID_SENSITIVE_MATERIALS -> 100.kilograms
        type in SlimefunTag.SENSITIVE_MATERIALS -> 100.kilograms
        !type.isCollidable -> 200.kilograms
        type == Material.IRON_BLOCK -> 7.9.tons
        type == Material.GOLD_BLOCK -> 19.3.tons
        type == Material.DIAMOND_BLOCK -> 3.5.tons
        type == Material.EMERALD_BLOCK -> 2.8.tons
        type == Material.QUARTZ_BLOCK -> 2.6.tons
        type == Material.NETHERITE_BLOCK -> 19.3.tons
        type == Material.GRANITE -> 2.6.tons
        type == Material.DIORITE -> 2.8.tons
        type == Material.ANDESITE -> 2.4.tons
        type == Material.CALCITE -> 2.7.tons
        type == Material.STONE || type == Material.COBBLESTONE -> 2.7.tons
        type in SlimefunTag.WOODEN_DOORS
                || type in SlimefunTag.WOODEN_SLABS
                || type in SlimefunTag.WOODEN_STAIRS
                || type in SlimefunTag.WOODEN_TRAPDOORS
                || type in SlimefunTag.WOODEN_FENCES
                || type in SlimefunTag.WOODEN_PRESSURE_PLATES
                || type in SlimefunTag.WOODEN_BUTTONS
                -> 0.3.tons
        type in SlimefunTag.LOGS || type in SlimefunTag.PLANKS -> 0.6.tons
        else -> 2.5.tons
    }