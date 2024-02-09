package io.github.addoncommunity.galactifun.serial

import io.github.addoncommunity.galactifun.util.decodeBase
import io.github.addoncommunity.galactifun.util.encodeBase
import io.github.seggan.kfun.serial.BlockStorageDataType

class PairBlockStorageDataType<A, B>(
    private val serializerA: BlockStorageDataType<A>,
    private val serializerB: BlockStorageDataType<B>
) : BlockStorageDataType<Pair<A, B>> {

    override fun serialize(value: Pair<A, B>): String {
        val sb = StringBuilder()
        val a = serializerA.serialize(value.first)
        sb.append(a.length.encodeBase(126))
        sb.append('\u007F')
        sb.append(a)
        sb.append(serializerB.serialize(value.second))
        return sb.toString()
    }

    override fun deserialize(value: String): Pair<A, B>? {
        val parts = value.split('\u007F', limit = 2)
        if (parts.size != 2) return null
        val length = parts[0].decodeBase(126)
        val a = parts[1].substring(0, length)
        val b = parts[1].substring(length)
        return serializerA.deserialize(a)?.let { da ->
            serializerB.deserialize(b)?.let { db ->
                da to db
            }
        }
    }
}