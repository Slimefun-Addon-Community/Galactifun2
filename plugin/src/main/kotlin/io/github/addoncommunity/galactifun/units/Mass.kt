package io.github.addoncommunity.galactifun.units

@JvmInline
value class Mass private constructor(val kilograms: Double) : Comparable<Mass> {

    companion object {
        val Double.kilograms: Mass
            get() = Mass(this)
    }

    operator fun plus(other: Mass): Mass = Mass(kilograms + other.kilograms)
    operator fun minus(other: Mass): Mass = Mass(kilograms - other.kilograms)
    operator fun times(scalar: Double): Mass = Mass(kilograms * scalar)
    operator fun div(scalar: Double): Mass = Mass(kilograms / scalar)
    operator fun rem(scalar: Double): Mass = Mass(kilograms % scalar)
    operator fun unaryMinus() = Mass(-kilograms)
    operator fun unaryPlus() = this

    override fun compareTo(other: Mass): Int = kilograms.compareTo(other.kilograms)
}