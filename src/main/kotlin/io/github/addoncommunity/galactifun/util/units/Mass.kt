package io.github.addoncommunity.galactifun.util.units

@JvmInline
value class Mass private constructor(val kilograms: Double) : Comparable<Mass> {

    companion object {
        val Double.kilograms: Mass
            get() = Mass(this)

        val Long.kilograms: Mass
            get() = this.toDouble().kilograms

        val Int.kilograms: Mass
            get() = this.toDouble().kilograms
    }

    operator fun plus(other: Mass): Mass {
        return Mass(kilograms + other.kilograms)
    }

    operator fun minus(other: Mass): Mass {
        return Mass(kilograms - other.kilograms)
    }

    operator fun times(other: Double): Mass {
        return Mass(kilograms * other)
    }

    operator fun times(other: Int): Mass {
        return Mass(kilograms * other)
    }

    operator fun times(other: Long): Mass {
        return Mass(kilograms * other)
    }

    operator fun div(other: Double): Mass {
        return Mass(kilograms / other)
    }

    operator fun div(other: Int): Mass {
        return Mass(kilograms / other)
    }

    operator fun div(other: Long): Mass {
        return Mass(kilograms / other)
    }

    override fun compareTo(other: Mass): Int {
        return kilograms.compareTo(other.kilograms)
    }
}