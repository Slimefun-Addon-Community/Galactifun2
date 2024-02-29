package io.github.seggan.uom

import kotlin.reflect.KClass

@Repeatable
@Target(AnnotationTarget.CLASS)
annotation class MultipliesTo(val other: KClass<*>, val product: KClass<*>)

@Repeatable
@Target(AnnotationTarget.CLASS)
annotation class DividesTo(val other: KClass<*>, val quotient: KClass<*>)

@Target(AnnotationTarget.CLASS)
annotation class Measure(val base: String)

@Repeatable
@Target(AnnotationTarget.CLASS)
annotation class AlternateUnit(val name: String, val ratio: Double)
