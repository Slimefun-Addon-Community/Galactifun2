package io.github.seggan.uom

import kotlin.reflect.KClass

@Repeatable
@Target(AnnotationTarget.CLASS)
annotation class MultipliesTo(val multiplicand: KClass<*>, val product: KClass<*>)

@Repeatable
@Target(AnnotationTarget.CLASS)
annotation class DividesTo(val divisor: KClass<*>, val quotient: KClass<*>)

@Repeatable
@Target(AnnotationTarget.CLASS)
annotation class AlternateUnit(val name: String, val ratio: Double)

@Target(AnnotationTarget.CLASS)
annotation class Measure(val base: String)
