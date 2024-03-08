package io.github.addoncommunity.galactifun.util.general

sealed interface Either<A, B> {

    data class Left<A, B>(val value: A) : Either<A, B> {
        override fun toString(): String = value.toString()
    }

    data class Right<A, B>(val value: B) : Either<A, B> {
        override fun toString(): String = value.toString()
    }
}