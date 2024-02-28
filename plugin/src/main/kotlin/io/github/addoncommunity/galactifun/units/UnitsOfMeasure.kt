@file:Suppress("unused")

package io.github.addoncommunity.galactifun.units

import io.github.addoncommunity.galactifun.Constants
import io.github.addoncommunity.galactifun.units.Angle.Companion.radians
import io.github.addoncommunity.galactifun.units.Distance.Companion.meters
import io.github.addoncommunity.galactifun.units.Velocity.Companion.metersPerSecond
import io.github.seggan.uom.AlternateUnit
import io.github.seggan.uom.Measure
import kotlin.math.PI
import kotlin.time.Duration

@Measure(base = "meters")
@AlternateUnit(name = "lightYears", ratio = Constants.KM_PER_LY * 1000)
@AlternateUnit(name = "kilometers", ratio = 1000.0)
@AlternateUnit(name = "au", ratio = Constants.KM_PER_AU * 1000)
private class ADistance

@Measure(base = "kilograms")
@AlternateUnit(name = "pounds", ratio = 2.20462)
private class AMass

@Measure(base = "metersPerSecond")
private class AVelocity
operator fun Distance.div(time: Duration): Velocity = (meters / time.doubleSeconds).metersPerSecond
operator fun Velocity.times(time: Duration): Distance = (metersPerSecond * time.doubleSeconds).meters

@Measure(base = "radians")
@AlternateUnit(name = "degrees", ratio = 180.0 / Math.PI)
private class AAngle

val Angle.standardForm: Angle
    get() = ((radians % (2 * PI) + 2 * PI) % (2 * PI)).radians

fun sin(angle: Angle): Double = kotlin.math.sin(angle.radians)
fun cos(angle: Angle): Double = kotlin.math.cos(angle.radians)
fun tan(angle: Angle): Double = kotlin.math.tan(angle.radians)
fun sinh(angle: Angle): Double = kotlin.math.sinh(angle.radians)
fun cosh(angle: Angle): Double = kotlin.math.cosh(angle.radians)
fun tanh(angle: Angle): Double = kotlin.math.tanh(angle.radians)
