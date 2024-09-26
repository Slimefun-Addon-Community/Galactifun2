package io.github.addoncommunity.galactifun.impl.items

import com.destroystokyo.paper.ParticleBuilder
import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.addoncommunity.galactifun.api.betteritem.BetterSlimefunItem
import io.github.addoncommunity.galactifun.api.betteritem.ItemHandler
import io.github.addoncommunity.galactifun.api.betteritem.Ticker
import io.github.addoncommunity.galactifun.impl.GalactifunHeads
import io.github.addoncommunity.galactifun.pluginInstance
import io.github.addoncommunity.galactifun.util.SlimefunStructure
import io.github.addoncommunity.galactifun.util.bukkit.*
import io.github.addoncommunity.galactifun.util.getValue
import io.github.addoncommunity.galactifun.util.items.materialType
import io.github.addoncommunity.galactifun.util.menu.buildMenu
import io.github.seggan.sf4k.extensions.div
import io.github.seggan.sf4k.extensions.plus
import io.github.seggan.sf4k.extensions.plusAssign
import io.github.seggan.sf4k.extensions.times
import io.github.seggan.sf4k.serial.blockstorage.getBlockStorage
import io.github.seggan.sf4k.serial.blockstorage.setBlockStorage
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun
import io.github.thebusybiscuit.slimefun4.utils.HeadTexture
import me.mrCookieSlime.Slimefun.api.BlockStorage
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.Structure
import org.bukkit.block.structure.UsageMode
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BlockVector
import org.bukkit.util.Vector
import kotlin.io.path.*

class SlimefunStructureBlock(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>
) : BetterSlimefunItem(itemGroup, item, recipeType, recipe) {

    private val particlesPerBlock: Int by itemSetting("particles-per-block", 3)

    companion object {

        private const val KEY_NAME = "name"
        private const val KEY_LENGTH = "length"
        private const val KEY_WIDTH = "width"
        private const val KEY_HEIGHT = "height"
        private const val KEY_OFFSET_X = "offset_x"
        private const val KEY_OFFSET_Y = "offset_y"
        private const val KEY_OFFSET_Z = "offset_z"

        private val menu = buildMenu {
            +"........."
            +"..s.n.o.."
            +"........."
            +".lhw.xyz."
            +"........."

            background('.')

            's' means item {
                name = "Save"
                material = GalactifunHeads.FLOPPY_DISK.materialType

                +""
                +"Save the current structure"
                +"with the given name"

                onClick { p, _ ->
                    p.closeInventory()
                    val structure = SlimefunStructure()
                    val size = getSize(block)
                    val offset = getOffset(block)
                    val offsetLocation = block.location + offset
                    structure.fill(offsetLocation, size, true)

                    val name = block.getBlockStorage<String>(KEY_NAME)
                    if (name.isNullOrBlank()) {
                        p.sendMessage(NamedTextColor.RED + "Please set a name first")
                        return@onClick
                    }
                    pluginInstance.structuresFolder.resolve("$name.nbt")
                        .apply { deleteIfExists() }
                        .createFile()
                        .outputStream()
                        .use(structure::saveToStream)

                    p.sendMessage(NamedTextColor.GREEN + "Saved structure $name")
                }
            }

            'o' means item {
                name = "Load"
                material = GalactifunHeads.FILE_EXPLORER.materialType

                +""
                +"Load a stored structure"
                +"with the given name"

                onClick { p, _ ->
                    p.closeInventory()
                    val offset = getOffset(block)
                    val offsetLocation = block.location + offset

                    val name = block.getBlockStorage<String>(KEY_NAME)
                    if (name.isNullOrBlank()) {
                        p.sendMessage(NamedTextColor.RED + "Please set a name first")
                        return@onClick
                    }
                    val structureFile = pluginInstance.structuresFolder.resolve("$name.nbt")
                    if (!structureFile.exists()) {
                        p.sendMessage(NamedTextColor.RED + "No structure found with name $name")
                        return@onClick
                    }
                    val structure = structureFile.inputStream().use(SlimefunStructure::loadFromStream)
                    structure.placeDefault(offsetLocation)

                    p.sendMessage(NamedTextColor.GREEN + "Loaded structure with key $name")
                }
            }

            'n' means item {
                name = "Name: "
                material = HeadTexture.SCRIPT_NEW.asItemStack.materialType

                +""
                +"Click to rename"

                onClick { p, _ ->
                    pluginInstance.launch {
                        p.closeInventory()
                        p.sendMessage("Type the new name in chat")
                        val name = p.awaitChatInput()
                        if (!name.all { it.isLetterOrDigit() || it == '_' }) {
                            p.sendMessage(NamedTextColor.RED + "Name can only contain letters, digits and underscores")
                            return@launch
                        }
                        block.setBlockStorage(KEY_NAME, name)
                        thisItem.editMeta { it.displayName("<white>Name: $name".miniComponent()) }
                        p.sendMessage(NamedTextColor.GREEN + "Set name to $name")
                    }
                }

                init {
                    val name = block.getBlockStorage<String>(KEY_NAME) ?: ""
                    thisItem.editMeta { it.displayName("<white>Name: $name".miniComponent()) }
                }
            }

            // <editor-fold desc="Bounding box" defaultstate="collapsed">

            'l' means item {
                name = "Length: 1"
                material = GalactifunHeads.LETTER_L.materialType

                +""
                +"Left click to increase"
                +"Right click to decrease"

                onClick { _, action ->
                    val addend = if (action.isRightClicked) -1 else 1
                    val length = (block.getBlockStorage<Int>(KEY_LENGTH) ?: 1) + addend
                    if (length < 1) return@onClick
                    block.setBlockStorage(KEY_LENGTH, length)
                    thisItem.editMeta { it.displayName("<white>Length: $length".miniComponent()) }
                }

                init {
                    val length = block.getBlockStorage<Int>(KEY_LENGTH) ?: 1
                    thisItem.editMeta { it.displayName("<white>Length: $length".miniComponent()) }
                }
            }

            'w' means item {
                name = "Width: 1"
                material = GalactifunHeads.LETTER_W.materialType

                +""
                +"Left click to increase"
                +"Right click to decrease"

                onClick { _, action ->
                    val addend = if (action.isRightClicked) -1 else 1
                    val width = (block.getBlockStorage<Int>(KEY_WIDTH) ?: 1) + addend
                    if (width < 1) return@onClick
                    block.setBlockStorage(KEY_WIDTH, width)
                    thisItem.editMeta { it.displayName("<white>Width: $width".miniComponent()) }
                }

                init {
                    val width = block.getBlockStorage<Int>(KEY_WIDTH) ?: 1
                    thisItem.editMeta { it.displayName("<white>Width: $width".miniComponent()) }
                }
            }

            'h' means item {
                name = "Height: 1"
                material = GalactifunHeads.LETTER_H.materialType

                +""
                +"Left click to increase"
                +"Right click to decrease"

                onClick { _, action ->
                    val addend = if (action.isRightClicked) -1 else 1
                    val height = (block.getBlockStorage<Int>(KEY_HEIGHT) ?: 1) + addend
                    if (height < 1) return@onClick
                    block.setBlockStorage(KEY_HEIGHT, height)
                    thisItem.editMeta { it.displayName("<white>Height: $height".miniComponent()) }
                }

                init {
                    val height = block.getBlockStorage<Int>(KEY_HEIGHT) ?: 1
                    thisItem.editMeta { it.displayName("<white>Height: $height".miniComponent()) }
                }
            }
            // </editor-fold>

            // <editor-fold desc="Offset" defaultstate="collapsed">

            'x' means item {
                name = "Offset x: 0"
                material = GalactifunHeads.LETTER_X.materialType

                +""
                +"Left click to increase"
                +"Right click to decrease"

                onClick { _, action ->
                    val addend = if (action.isRightClicked) -1 else 1
                    val x = (block.getBlockStorage<Int>(KEY_OFFSET_X) ?: 0) + addend
                    block.setBlockStorage(KEY_OFFSET_X, x)
                    thisItem.editMeta { it.displayName("<white>Offset x: $x".miniComponent()) }
                }

                init {
                    val x = block.getBlockStorage<Int>(KEY_OFFSET_X) ?: 0
                    thisItem.editMeta { it.displayName("<white>Offset x: $x".miniComponent()) }
                }
            }

            'y' means item {
                name = "Offset y: 1"
                material = GalactifunHeads.LETTER_Y.materialType

                +""
                +"Left click to increase"
                +"Right click to decrease"

                onClick { _, action ->
                    val addend = if (action.isRightClicked) -1 else 1
                    val y = (block.getBlockStorage<Int>(KEY_OFFSET_Y) ?: 1) + addend
                    block.setBlockStorage(KEY_OFFSET_Y, y)
                    thisItem.editMeta { it.displayName("<white>Offset y: $y".miniComponent()) }
                }

                init {
                    val y = block.getBlockStorage<Int>(KEY_OFFSET_Y) ?: 1
                    thisItem.editMeta { it.displayName("<white>Offset y: $y".miniComponent()) }
                }
            }

            'z' means item {
                name = "Offset z: 0"
                material = GalactifunHeads.LETTER_Z.materialType

                +""
                +"Left click to increase"
                +"Right click to decrease"

                onClick { _, action ->
                    val addend = if (action.isRightClicked) -1 else 1
                    val z = (block.getBlockStorage<Int>(KEY_OFFSET_Z) ?: 0) + addend
                    block.setBlockStorage(KEY_OFFSET_Z, z)
                    thisItem.editMeta { it.displayName("<white>Offset z: $z".miniComponent()) }
                }

                init {
                    val z = block.getBlockStorage<Int>(KEY_OFFSET_Z) ?: 0
                    thisItem.editMeta { it.displayName("<white>Offset z: $z".miniComponent()) }
                }
            }
            // </editor-fold>
        }

        private fun getSize(block: Block): BlockVector {
            val length = block.getBlockStorage<Int>(KEY_LENGTH) ?: 1
            val width = block.getBlockStorage<Int>(KEY_WIDTH) ?: 1
            val height = block.getBlockStorage<Int>(KEY_HEIGHT) ?: 1
            return BlockVector(length, height, width)
        }

        private fun getOffset(block: Block): BlockVector {
            val offsetX = block.getBlockStorage<Int>(KEY_OFFSET_X) ?: 0
            val offsetY = block.getBlockStorage<Int>(KEY_OFFSET_Y) ?: 1
            val offsetZ = block.getBlockStorage<Int>(KEY_OFFSET_Z) ?: 0
            return BlockVector(offsetX, offsetY, offsetZ)
        }
    }

    @ItemHandler(BlockPlaceHandler::class)
    private fun onPlace(e: BlockPlaceEvent) {
        val state = e.block.state as Structure
        state.usageMode = UsageMode.SAVE
        state.update()
    }

    @ItemHandler(BlockUseHandler::class)
    private fun onClick(e: PlayerRightClickEvent) {
        e.cancel()
        pluginInstance.launch {
            delayTicks(1)
            BlockStorage.getInventory(e.clickedBlock.orElseThrow()).open(e.player)
        }
    }

    @Ticker
    private fun tick(b: Block) {
        drawBoundingBox(b)
    }

    private fun drawBoundingBox(block: Block) {
        val size = getSize(block)
        val offset = getOffset(block)

        val start = block.location + offset
        val steps = size * particlesPerBlock

        for ((axes, color) in AXES) {
            val (axis, axis1, axis2) = axes
            val stepVector = axis / particlesPerBlock
            val startPoints = listOf(
                start,
                start + (size * axis1),
                start + (size * axis2),
                start + size * (axis1 + axis2),
            )
            var colored = false
            for (startPoint in startPoints) {
                val lineColor = if (colored) Color.WHITE else color
                generateParticles(
                    startPoint,
                    stepVector * axis,
                    (steps * axis).length().toInt(),
                    lineColor
                )
                colored = true
            }
        }
    }

    private fun generateParticles(start: Location, step: Vector, steps: Int, color: Color) {
        val locations = mutableListOf<Location>()
        val current = start.clone()
        repeat(steps) {
            locations.add(current.clone())
            current += step
        }
        locations.consumeSpreadOut(Slimefun.getTickerTask().tickRate) { location ->
            ParticleBuilder(Particle.DUST)
                .count(1)
                .location(location)
                .color(color)
                .spawn()
        }
    }

    override fun postRegister() {
        menu.apply(this)
        isHidden = true
    }
}

private val AXES = listOf(
    listOf(UnitVector.X, UnitVector.Y, UnitVector.Z) to Color.RED,
    listOf(UnitVector.Y, UnitVector.X, UnitVector.Z) to Color.GREEN,
    listOf(UnitVector.Z, UnitVector.X, UnitVector.Y) to Color.BLUE,
)