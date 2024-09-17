package io.github.addoncommunity.galactifun.units

import io.github.addoncommunity.galactifun.TAU
import io.github.addoncommunity.galactifun.units.Angle.Companion.radians

val Angle.standardForm: Angle
    get() = ((radians % TAU + TAU) % TAU).radians

fun sin(angle: Angle): Double = kotlin.math.sin(angle.radians)
fun cos(angle: Angle): Double = kotlin.math.cos(angle.radians)
fun tan(angle: Angle): Double = kotlin.math.tan(angle.radians)
fun sinh(angle: Angle): Double = kotlin.math.sinh(angle.radians)
fun cosh(angle: Angle): Double = kotlin.math.cosh(angle.radians)
fun tanh(angle: Angle): Double = kotlin.math.tanh(angle.radians)
