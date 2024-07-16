package io.github.addoncommunity.galactifun.impl.items

import io.github.addoncommunity.galactifun.api.rockets.RocketInfo
import io.github.addoncommunity.galactifun.impl.items.abstract.Seat
import io.github.addoncommunity.galactifun.impl.managers.PlanetManager
import io.github.addoncommunity.galactifun.impl.managers.RocketManager
import io.github.addoncommunity.galactifun.util.bukkit.plus
import io.github.seggan.sf4k.serial.blockstorage.getBlockStorage
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CaptainsChair(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>
) : Seat(itemGroup, item, recipeType, recipe) {

    override fun onSit(p: Player, b: Block) {
        val rocket = getRocket(b)
        if (rocket == null) {
            p.sendMessage(NamedTextColor.RED + "This chair hasn't connected to the rocket yet, please make sure the rocket has a command computer and try again in a few seconds.")
            return
        }
        p.sendMessage(NamedTextColor.GOLD + rocket.info)
        val currentPlanet = PlanetManager.getByWorld(p.world) ?: return
//        object : PlanetMenu() {
//            override fun modifyItem(p: Player, obj: CelestialObject, item: ItemStack): ItemStack {
//                item.modifyLore {
//                    if (obj == currentPlanet) {
//                        it.add(NamedTextColor.GREEN + "You are here")
//                    }
//                    if (obj.orbiters.isNotEmpty()) {
//                        it.add(NamedTextColor.YELLOW + "Left click to view orbiters")
//                    }
//                    if (obj is AlienWorld) {
//                        it.add(NamedTextColor.YELLOW + "Right click to travel to this planet")
//                    }
//                }
//                return item
//            }
//
//            override fun onClick(p: Player, obj: CelestialObject, action: ClickAction): Boolean {
//                if (action.isRightClicked && obj is PlanetaryWorld) {
//                    rocket.travelTo(obj)
//                    return false
//                }
//                return true
//            }
//        }
    }

    private fun getRocket(b: Block): RocketInfo? {
        val commandComputer = b.getBlockStorage<BlockPosition>("rocket") ?: return null
        return RocketManager.getInfo(commandComputer)
    }
}