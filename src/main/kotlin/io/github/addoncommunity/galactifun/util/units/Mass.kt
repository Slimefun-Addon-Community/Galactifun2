package io.github.addoncommunity.galactifun.util.units

@JvmInline
value class Mass private constructor(val tons: Double) : Comparable<Mass> {

    val kilograms: Long
        get() = (tons * 1000).toLong()

    companion object {
        val Double.kilograms: Mass
            get() = Mass(this / 1000)

        val Long.kilograms: Mass
            get() = Mass(this / 1000.0)

        val Int.kilograms: Mass
            get() = Mass(this / 1000.0)

        val Double.tons: Mass
            get() = Mass(this)

        val Long.tons: Mass
            get() = Mass(this.toDouble())

        val Int.tons: Mass
            get() = Mass(this.toDouble())
    }

    operator fun plus(other: Mass): Mass {
        return Mass(tons + other.tons)
    }

    operator fun minus(other: Mass): Mass {
        return Mass(tons - other.tons)
    }

    operator fun times(other: Double): Mass {
        return Mass(tons * other)
    }

    operator fun times(other: Int): Mass {
        return Mass(tons * other)
    }

    operator fun times(other: Long): Mass {
        return Mass(tons * other)
    }

    operator fun div(other: Double): Mass {
        return Mass(tons / other)
    }

    operator fun div(other: Int): Mass {
        return Mass(tons / other)
    }

    operator fun div(other: Long): Mass {
        return Mass(tons / other)
    }

    override fun compareTo(other: Mass): Int {
        return tons.compareTo(other.tons)
    }


}