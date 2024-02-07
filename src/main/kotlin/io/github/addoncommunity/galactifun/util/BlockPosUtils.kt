package io.github.addoncommunity.galactifun.util

import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

fun BlockPosition.getFace(face: BlockFace): BlockPosition {
    return BlockPosition(this.world, this.x + face.modX, this.y + face.modY, this.z + face.modZ)
}

@PublishedApi
internal val searchFaces = arrayOf(
    BlockFace.UP,
    BlockFace.DOWN,
    BlockFace.NORTH,
    BlockFace.SOUTH,
    BlockFace.EAST,
    BlockFace.WEST
)

inline fun BlockPosition.floodSearch(searchLimit: Int = 1024, predicate: Block.(Block) -> Boolean): FloodSearchResult {
    var toSearch = mutableSetOf(this)
    var toSearchNext = mutableSetOf<BlockPosition>()
    val found = mutableSetOf<BlockPosition>()
    while (true) {
        if (toSearch.isEmpty()) {
            return FloodSearchResult(found, false)
        }
        for (pos in toSearch) {
            if (pos in found) continue
            found.add(pos)
            if (found.size >= searchLimit) {
                return FloodSearchResult(found, true)
            }
            val block = pos.block
            for (face in searchFaces) {
                val next = pos.getFace(face)
                if (next !in found && next !in toSearch && next !in toSearchNext) {
                    if (block.predicate(next.block)) {
                        toSearchNext.add(next)
                    }
                }
            }
        }
        toSearch = toSearchNext
        toSearchNext = mutableSetOf()
    }
}

data class FloodSearchResult(val found: Set<BlockPosition>, val exceededMax: Boolean)