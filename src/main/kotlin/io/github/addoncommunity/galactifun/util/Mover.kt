package io.github.addoncommunity.galactifun.util

import com.jeff_media.morepersistentdatatypes.DataType
import me.mrCookieSlime.Slimefun.api.BlockStorage
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.block.Sign
import org.bukkit.block.Skull
import org.bukkit.block.sign.Side
import org.bukkit.entity.BlockDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType


private val sfDataKey = "sfData".toKey()
private val extraDataKey = "extraData".toKey()
private val extraData2Key = "extraData2".toKey()

private val bytesListPdt = DataType.asList(DataType.BYTE_ARRAY)
private val stringListPdt = DataType.asList(DataType.STRING)

fun Block.convertToDisplayEntity(): BlockDisplay {
    val location = location
    val world = location.world
    val display = world.spawn<BlockDisplay>(location)

    display.block = this.blockData

    val pdc = display.persistentDataContainer
    pdc[sfDataKey, PersistentDataType.STRING] = BlockStorage.getBlockInfoAsJson(this)

    val state = this.state
    if (state is Container) {
        pdc[extraDataKey, bytesListPdt] = state.inventory.contents.map {
            it?.serializeAsBytes() ?: byteArrayOf()
        }
    }
    if (state is Sign) {
        pdc[extraDataKey, stringListPdt] = state.getSide(Side.FRONT)
            .lines()
            .map(JSONComponentSerializer.json()::serialize)
        pdc[extraData2Key, stringListPdt] = state.getSide(Side.BACK)
            .lines()
            .map(JSONComponentSerializer.json()::serialize)
    }
    if (state is Skull) {
        val player = state.owningPlayer
        if (player != null) {
            pdc[extraDataKey, DataType.UUID] = player.uniqueId
        }
    }

    this.type = Material.AIR
    BlockStorage.clearBlockInfo(this)
    return display
}

fun BlockDisplay.convertToBlock(): Block {
    val location = location.toBlockLocation()
    val world = location.world
    val block = world.getBlockAt(location)

    block.blockData = this.block

    val pdc = persistentDataContainer
    val blockInfo = pdc[sfDataKey, PersistentDataType.STRING]
    if (blockInfo != null) {
        BlockStorage.setBlockInfo(block, blockInfo, true)
    }

    val state = block.state
    if (state is Container) {
        val contents = pdc[extraDataKey, bytesListPdt]
        if (contents != null) {
            state.inventory.contents = contents.map {
                if (it.isEmpty()) null else ItemStack.deserializeBytes(it)
            }.toTypedArray()
        }
    }
    if (state is Sign) {
        val front = pdc[extraDataKey, stringListPdt] ?: emptyList()
        val back = pdc[extraData2Key, stringListPdt] ?: emptyList()

        val frontSide = state.getSide(Side.FRONT)
        for ((i, line) in front.withIndex()) {
            frontSide.line(i, JSONComponentSerializer.json().deserialize(line))
        }
        val backSide = state.getSide(Side.BACK)
        for ((i, line) in back.withIndex()) {
            backSide.line(i, JSONComponentSerializer.json().deserialize(line))
        }
    }
    if (state is Skull) {
        val uuid = pdc[extraDataKey, DataType.UUID]
        if (uuid != null) {
            // What is wrong with you Kotlin???
            state.setOwningPlayer(Bukkit.getOfflinePlayer(uuid))
        }
    }

    remove()
    return block
}