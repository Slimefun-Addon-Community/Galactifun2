package io.github.addoncommunity.galactifun.util

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

val Double.years: Duration
    get() = (this * 365.25).days

val Int.years: Duration
    get() = (this * 365.25).days