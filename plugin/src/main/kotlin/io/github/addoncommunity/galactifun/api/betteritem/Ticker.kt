package io.github.addoncommunity.galactifun.api.betteritem

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Ticker(val async: Boolean = false)
