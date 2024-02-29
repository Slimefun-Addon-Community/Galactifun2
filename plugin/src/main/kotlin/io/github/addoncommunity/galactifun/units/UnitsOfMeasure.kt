@file:Suppress("unused")

package io.github.addoncommunity.galactifun.units

import io.github.addoncommunity.galactifun.Constants
import io.github.addoncommunity.galactifun.units.Acceleration.Companion.metersPerSecondSquared
import io.github.addoncommunity.galactifun.units.Distance.Companion.meters
import io.github.addoncommunity.galactifun.units.Velocity.Companion.metersPerSecond
import io.github.seggan.uom.AlternateUnit
import io.github.seggan.uom.DividesTo
import io.github.seggan.uom.Measure
import io.github.seggan.uom.MultipliesTo
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

@Measure(base = "metersPerSecondSquared")
@MultipliesTo(other = AMass::class, product = AForce::class)
private class AAcceleration
operator fun Velocity.div(time: Duration): Acceleration = (metersPerSecond / time.doubleSeconds).metersPerSecondSquared
operator fun Acceleration.times(time: Duration): Velocity = (metersPerSecondSquared * time.doubleSeconds).metersPerSecond

@Measure(base = "newtons")
@DividesTo(other = AAcceleration::class, quotient = AMass::class)
private class AForce