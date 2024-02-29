package io.github.seggan.uom

import kotlin.reflect.KClass

/**
 * A measure.
 *
 * @param base The base unit for this measure.
 */
@Target(AnnotationTarget.CLASS)
annotation class Measure(val base: String)

/**
 * An alternate unit for a measure.
 *
 * @param unit The alternate unit.
 * @param ratio How many alternate units are in the base unit.
 */
@Repeatable
@Target(AnnotationTarget.CLASS)
annotation class AlternateUnit(val unit: String, val ratio: Double)

/**
 * A measure that multiplies to another measure.
 *
 * @param other The other measure.
 * @param product The product measure.
 */
@Repeatable
@Target(AnnotationTarget.CLASS)
annotation class MultipliesTo(val other: KClass<*>, val product: KClass<*>)

/**
 * A measure that divides to another measure.
 *
 * @param other The other measure.
 * @param quotient The quotient measure.
 */
@Repeatable
@Target(AnnotationTarget.CLASS)
annotation class DividesTo(val other: KClass<*>, val quotient: KClass<*>)
