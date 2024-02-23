package io.github.addoncommunity.galactifun.units

import kotlin.math.*

@JvmInline
value class Angle private constructor(val radians: Double) : Comparable<Angle> {

    val degrees: Double
        get() = radians * DEGREES_PER_RADIAN

    val arcminutes: Double
        get() = degrees * 60

    val arcseconds: Double
        get() = arcminutes * 60

    val milliarcseconds: Double
        get() = arcseconds * 1000

    companion object {
        const val DEGREES_PER_RADIAN = 180 / Math.PI

        val Double.degrees: Angle
            get() = (this / DEGREES_PER_RADIAN).radians

        val Double.arcminutes: Angle
            get() = (this / 60).degrees

        val Double.arcseconds: Angle
            get() = (this / 60).arcminutes

        val Double.milliarcseconds: Angle
            get() = (this / 1000).arcseconds

        val Double.radians: Angle
            get() = Angle(this)
    }

    operator fun plus(other: Angle): Angle = Angle(radians + other.radians)
    operator fun minus(other: Angle): Angle = Angle(radians - other.radians)
    operator fun times(scalar: Double): Angle = Angle(radians * scalar)
    operator fun div(scalar: Double): Angle = Angle(radians / scalar)
    operator fun rem(scalar: Double): Angle = Angle(radians % scalar)
    operator fun unaryMinus() = Angle(-radians)
    operator fun unaryPlus() = this

    override fun compareTo(other: Angle): Int = radians.compareTo(other.radians)
}

fun sin(angle: Angle): Double = sin(angle.radians)
fun cos(angle: Angle): Double = cos(angle.radians)
fun tan(angle: Angle): Double = tan(angle.radians)
fun sinh(angle: Angle): Double = sinh(angle.radians)
fun cosh(angle: Angle): Double = cosh(angle.radians)
fun tanh(angle: Angle): Double = tanh(angle.radians)