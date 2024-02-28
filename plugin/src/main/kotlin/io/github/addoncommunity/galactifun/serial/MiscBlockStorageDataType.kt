package io.github.addoncommunity.galactifun.serial

import io.github.addoncommunity.galactifun.Constants
import io.github.seggan.kfun.serial.BlockStorageDataType
import kotlin.reflect.KClass

class PairBlockStorageDataType<A, B>(
    private val serializerA: BlockStorageDataType<A>,
    private val serializerB: BlockStorageDataType<B>
) : BlockStorageDataType<Pair<A, B>> {

    override fun serialize(value: Pair<A, B>): String {
        val sb = StringBuilder()
        val a = serializerA.serialize(value.first)
        sb.append(a.length.toString(Constants.MAX_RADIX))
        sb.append(' ')
        sb.append(a)
        sb.append(serializerB.serialize(value.second))
        return sb.toString()
    }

    override fun deserialize(value: String): Pair<A, B>? {
        val parts = value.split(' ', limit = 2)
        if (parts.size != 2) return null
        val length = parts[0].toInt(Constants.MAX_RADIX)
        val a = parts[1].substring(0, length)
        val b = parts[1].substring(length)
        return serializerA.deserialize(a)?.let { da ->
            serializerB.deserialize(b)?.let { db ->
                da to db
            }
        }
    }
}

class EnumBlockStorageDataType<E : Enum<E>>(enumClass: Class<E>) : BlockStorageDataType<E> {

    constructor(enumClass: KClass<E>) : this(enumClass.java)

    private val constants = enumClass.enumConstants.associateBy(Enum<E>::name)

    override fun serialize(value: E): String = value.name

    override fun deserialize(value: String): E? = constants[value]
}