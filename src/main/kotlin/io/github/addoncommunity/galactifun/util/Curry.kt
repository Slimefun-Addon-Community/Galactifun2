package io.github.addoncommunity.galactifun.util

fun <A, R> ((A) -> R).curry(a: A): () -> R = { this(a) }

fun <A, B, R> ((A, B) -> R).curry(a: A): (B) -> R = { b -> this(a, b) }

fun <A, B, C, R> ((A, B, C) -> R).curry(a: A): (B, C) -> R = { b, c -> this(a, b, c) }

fun <A, B, C, D, R> ((A, B, C, D) -> R).curry(a: A): (B, C, D) -> R = { b, c, d -> this(a, b, c, d) }

fun <A, B, C, D, E, R> ((A, B, C, D, E) -> R).curry(a: A): (B, C, D, E) -> R = { b, c, d, e -> this(a, b, c, d, e) }