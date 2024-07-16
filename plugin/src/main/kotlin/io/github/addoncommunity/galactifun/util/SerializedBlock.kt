package io.github.addoncommunity.galactifun.util

import io.github.addoncommunity.galactifun.util.bukkit.key
import io.github.addoncommunity.galactifun.util.bukkit.summon
import io.github.seggan.sf4k.serial.pdc.get
import io.github.seggan.sf4k.serial.pdc.set
import me.mrCookieSlime.Slimefun.api.BlockStorage
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.BlockDisplay
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.structure.Structure
import org.bukkit.util.BlockVector
import org.bukkit.util.BoundingBox
import java.io.ByteArrayOutputStream
import java.util.concurrent.ThreadLocalRandom

class SerializedBlock private constructor(
    private val data: BlockData,
    private val blockStorage: String,
    private val structure: Structure
) {

    companion object {

        private val SAVED_DATA = "saved_block_data".key()
        private val SAVED_STRUCTURE = "saved_structure".key()
        private val SAVED_BLOCK_STORAGE = "saved_block_storage".key()

        fun serialize(
            block: Block,
            delete: Boolean = true,
            includeEntities: Boolean = false
        ): SerializedBlock {
            val data = block.blockData.clone()
            val blockStorage = BlockStorage.getBlockInfoAsJson(block)
            val structure = Bukkit.getStructureManager().createStructure()
            structure.fill(block.location, BlockVector(1, 1, 1), includeEntities)
            if (delete) {
                block.type = Material.AIR
                BlockStorage.clearBlockInfo(block)
            }
            if (includeEntities) {
                for (entity in block.world.getNearbyEntities(BoundingBox.of(block))) {
                    entity.remove()
                }
            }
            return SerializedBlock(data, blockStorage, structure)
        }

        fun loadFromPdc(pdc: PersistentDataContainer): SerializedBlock? {
            val data = pdc.get<String>(SAVED_DATA)?.let(Bukkit::createBlockData) ?: return null
            val blockStorage = pdc.get<String>(SAVED_BLOCK_STORAGE) ?: return null
            val bytes = pdc.get<ByteArray>(SAVED_STRUCTURE) ?: return null
            val structure = Bukkit.getStructureManager().loadStructure(bytes.inputStream())
            return SerializedBlock(data, blockStorage, structure)
        }

        fun loadFromDisplayEntity(display: BlockDisplay, delete: Boolean = true): SerializedBlock? {
            val serialized = loadFromPdc(display.persistentDataContainer) ?: return null
            if (delete) {
                display.remove()
            }
            return serialized
        }
    }

    fun place(location: Location) {
        val block = location.block
        structure.place(
            location,
            true,
            StructureRotation.NONE,
            Mirror.NONE,
            0,
            1.0F,
            ThreadLocalRandom.current()
        )
        BlockStorage.setBlockInfo(block, blockStorage, true)
        // We don't need to restore the block data because the structure already does that
    }

    fun saveToPdc(pdc: PersistentDataContainer) {
        pdc.set(SAVED_DATA, data.asString)
        pdc.set(SAVED_BLOCK_STORAGE, blockStorage)
        val stream = ByteArrayOutputStream()
        Bukkit.getStructureManager().saveStructure(stream, structure)
        pdc.set(SAVED_STRUCTURE, stream.toByteArray())
    }

    fun createDisplayEntity(location: Location): BlockDisplay {
        val block = location.block
        val display = block.world.summon<BlockDisplay>(location)
        display.block = data
        saveToPdc(display.persistentDataContainer)
        return display
    }
}