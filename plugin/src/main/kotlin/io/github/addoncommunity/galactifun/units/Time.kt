package io.github.addoncommunity.galactifun.units

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit

inline val Double.years: Duration
    get() = (this * 365.25).days

inline val Int.years: Duration
    get() = this.toDouble().years

inline val Duration.doubleSeconds: Double
    get() = toDouble(DurationUnit.SECONDS)

fun Iterable<Duration>.unitSumOf(): Duration = fold(Duration.ZERO) { acc, duration -> acc + duration }