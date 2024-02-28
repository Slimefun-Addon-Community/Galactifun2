package io.github.addoncommunity.galactifun.units

import io.github.seggan.uom.AlternateUnit
import io.github.seggan.uom.Measure
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit

inline val Double.years: Duration
    get() = (this * 365.25).days

inline val Int.years: Duration
    get() = this.toDouble().years

inline val Duration.doubleSeconds: Double
    get() = toDouble(DurationUnit.SECONDS)

@Measure(base = "metersPerSecond")
@AlternateUnit(name = "kilometersPerHour", ratio = 3.6)
private class AVelocity