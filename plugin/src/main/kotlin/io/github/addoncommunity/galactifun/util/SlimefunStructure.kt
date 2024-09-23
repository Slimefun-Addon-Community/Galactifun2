package io.github.addoncommunity.galactifun.util

import io.github.addoncommunity.galactifun.util.bukkit.copy
import io.github.addoncommunity.galactifun.util.bukkit.getPdc
import io.github.addoncommunity.galactifun.util.bukkit.key
import io.github.addoncommunity.galactifun.util.bukkit.setPdc
import io.github.seggan.sf4k.extensions.div
import io.github.seggan.sf4k.extensions.minus
import io.github.seggan.sf4k.extensions.plus
import me.mrCookieSlime.Slimefun.api.BlockStorage
import org.bukkit.*
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import org.bukkit.structure.Structure
import org.bukkit.util.BlockTransformer
import org.bukkit.util.BlockVector
import org.bukkit.util.EntityTransformer
import org.bukkit.util.Vector
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.collections.set

@Suppress("UnstableApiUsage")
class SlimefunStructure(
    private val delegate: Structure = Bukkit.getStructureManager().createStructure()
) : Structure by delegate {

    private var center: Vector = getNewCenter()

    companion object {
        private val blockStorageKey = "block_storage".key()

        fun load(key: NamespacedKey): SlimefunStructure? {
            return Bukkit.getStructureManager().loadStructure(key)?.let(::SlimefunStructure)
        }

        fun loadFromStream(stream: InputStream): SlimefunStructure {
            return Bukkit.getStructureManager().loadStructure(stream).let(::SlimefunStructure)
        }
    }

    override fun place(
        location: Location,
        includeEntities: Boolean,
        structureRotation: StructureRotation,
        mirror: Mirror,
        palette: Int,
        integrity: Float,
        random: Random,
        blockTransformers: MutableCollection<BlockTransformer>,
        entityTransformers: MutableCollection<EntityTransformer>
    ) {
        place(
            location.world,
            location.toVector().toBlockVector(),
            includeEntities,
            structureRotation,
            mirror,
            palette,
            integrity,
            random,
            blockTransformers,
            entityTransformers
        )
    }

    override fun place(
        regionAccessor: RegionAccessor,
        location: BlockVector,
        includeEntities: Boolean,
        structureRotation: StructureRotation,
        mirror: Mirror,
        palette: Int,
        integrity: Float,
        random: Random
    ) {
        place(
            regionAccessor,
            location,
            includeEntities,
            structureRotation,
            mirror,
            palette,
            integrity,
            random,
            mutableListOf(),
            mutableListOf()
        )
    }

    override fun place(
        location: Location,
        includeEntities: Boolean,
        structureRotation: StructureRotation,
        mirror: Mirror,
        palette: Int,
        integrity: Float,
        random: Random
    ) {
        place(
            location,
            includeEntities,
            structureRotation,
            mirror,
            palette,
            integrity,
            random,
            mutableListOf(),
            mutableListOf()
        )
    }

    override fun place(
        regionAccessor: RegionAccessor,
        location: BlockVector,
        includeEntities: Boolean,
        structureRotation: StructureRotation,
        mirror: Mirror,
        palette: Int,
        integrity: Float,
        random: Random,
        blockTransformers: MutableCollection<BlockTransformer>,
        entityTransformers: MutableCollection<EntityTransformer>
    ) {
        val data = getPdc<Map<BlockVector, Pair<String, Material>>>(blockStorageKey) ?: emptyMap()
        val rotated = data.mapKeys { (vector, _) -> vector.applyStructureTransforms(structureRotation, mirror) }
        blockTransformers.add(BlockTransformer { region, x, y, z, current, _ ->
            val vector = BlockVector(x, y, z).subtract(location)
            val (json, material) = rotated[vector] ?: return@BlockTransformer current
            if (current.type != material) return@BlockTransformer current // Block has been changed
            val realLocation = Location(region.world, x.toDouble(), y.toDouble(), z.toDouble())
            BlockStorage.setBlockInfo(realLocation, json, true)
            return@BlockTransformer current
        })
        delegate.place(
            regionAccessor,
            location,
            includeEntities,
            structureRotation,
            mirror,
            palette,
            integrity,
            random,
            blockTransformers,
            entityTransformers
        )
    }

    override fun fill(origin: Location, size: BlockVector, includeEntities: Boolean) {
        val data = mutableMapOf<BlockVector, Pair<String, Material>>()
        for (x in 0 until size.blockX) {
            for (y in 0 until size.blockY) {
                for (z in 0 until size.blockZ) {
                    val vector = BlockVector(x, y, z)
                    val block = (origin + vector).block
                    if (BlockStorage.hasBlockInfo(block)) {
                        data[vector] = BlockStorage.getBlockInfoAsJson(block) to block.type
                    }
                }
            }
        }
        delegate.fill(origin, size, includeEntities)
        persistentDataContainer.remove(blockStorageKey)
        setPdc(blockStorageKey, data)
        center = getNewCenter()
    }

    override fun fill(corner1: Location, corner2: Location, includeEntities: Boolean) {
        fill(corner1, corner2.clone().subtract(corner1).toVector().toBlockVector(), includeEntities)
    }

    fun save(key: NamespacedKey) {
        Bukkit.getStructureManager().saveStructure(key, delegate)
    }

    fun saveToStream(stream: OutputStream) {
        Bukkit.getStructureManager().saveStructure(stream, delegate)
    }

    private fun getNewCenter(): Vector {
        val center = size / 2
        return center.setY(0)
    }

    private fun Vector.applyStructureTransforms(rotation: StructureRotation, mirror: Mirror): BlockVector {
        val centered = this - center
        val rotated = when (rotation) {
            StructureRotation.NONE -> centered
            StructureRotation.CLOCKWISE_90 -> centered.copy(x = -centered.z, z = centered.x)
            StructureRotation.CLOCKWISE_180 -> centered.copy(x = -centered.x, z = -centered.z)
            StructureRotation.COUNTERCLOCKWISE_90 -> centered.copy(x = centered.z, z = -centered.x)
        }
        val mirrored = when (mirror) {
            Mirror.NONE -> rotated
            Mirror.FRONT_BACK -> rotated.copy(x = -rotated.x)
            Mirror.LEFT_RIGHT -> rotated.copy(z = -rotated.z)
        }
        return mirrored.add(center).toBlockVector()
    }
}