package io.github.addoncommunity.galactifun.impl

import io.github.addoncommunity.galactifun.util.key
import io.github.thebusybiscuit.slimefun4.api.items.groups.NestedItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.groups.SubItemGroup
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack
import org.bukkit.Material

object GalactifunCategories {

    val MAIN = NestedItemGroup(
        "main".key(),
        CustomItemStack(Material.SUNFLOWER, "&fGalactifun")
    )

    val ROCKET_COMPONENTS = SubItemGroup(
        "rocket_components".key(),
        MAIN,
        CustomItemStack(Material.BLAZE_ROD, "&fRocket Components")
    )

    val GASES = SubItemGroup(
        "gases".key(),
        MAIN,
        CustomItemStack(Material.BONE_MEAL, "&fGases")
    )
}