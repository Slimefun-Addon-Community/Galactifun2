package io.github.addoncommunity.galactifun.util.general

data class IntPair(val first: Int, val second: Int)

infix fun Int.with(that: Int) = IntPair(this, that)
