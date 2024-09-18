package io.github.addoncommunity.galactifun.util

import io.github.addoncommunity.galactifun.util.bukkit.copy
import io.github.addoncommunity.galactifun.util.bukkit.key
import io.github.seggan.sf4k.serial.pdc.get
import io.github.seggan.sf4k.serial.pdc.set
import me.mrCookieSlime.Slimefun.api.BlockStorage
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.RegionAccessor
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import org.bukkit.structure.Structure
import org.bukkit.util.BlockTransformer
import org.bukkit.util.BlockVector
import org.bukkit.util.EntityTransformer
import org.bukkit.util.Vector
import java.util.*
import kotlin.collections.set

@Suppress("UnstableApiUsage")
class SlimefunStructure(private val delegate: Structure) : Structure by delegate {

    private var center: Vector = resetCenter()

    companion object {
        private val blockStorageKey = "block_storage".key()
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
        val data = persistentDataContainer.get<Map<BlockVector, Pair<String, Material>>>(blockStorageKey) ?: emptyMap()
        val rotated = data.mapKeys { (vector, _) ->
            val newVec = structureRotation.rotateAroundCenter(vector)
            when (mirror) {
                Mirror.NONE -> newVec
                Mirror.FRONT_BACK -> newVec.copy(x = -newVec.x)
                Mirror.LEFT_RIGHT -> newVec.copy(z = -newVec.z)
            }
        }
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
                    val block = origin.clone().add(vector).block
                    if (BlockStorage.hasBlockInfo(block)) {
                        data[vector] = BlockStorage.getBlockInfoAsJson(block) to block.type
                    }
                }
            }
        }
        delegate.fill(origin, size, includeEntities)
        persistentDataContainer.set(blockStorageKey, data)
        center = resetCenter()
    }

    override fun fill(corner1: Location, corner2: Location, includeEntities: Boolean) {
        fill(corner1, corner2.clone().subtract(corner1).toVector().toBlockVector(), includeEntities)
    }

    private fun resetCenter(): Vector {
        return size.clone().multiply(0.5).setY(0)
    }

    private fun StructureRotation.rotateAroundCenter(location: Vector): BlockVector {
        val centered = location.clone().subtract(center)
        val rotated = when (this) {
            StructureRotation.NONE -> centered
            StructureRotation.CLOCKWISE_90 -> centered.copy(x = -centered.z, z = centered.x)
            StructureRotation.CLOCKWISE_180 -> centered.copy(x = -centered.x, z = -centered.z)
            StructureRotation.COUNTERCLOCKWISE_90 -> centered.copy(x = centered.z, z = -centered.x)
        }
        return rotated.add(center).toBlockVector()
    }
}