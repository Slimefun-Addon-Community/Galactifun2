package io.github.addoncommunity.galactifun.api.objects

object MilkyWay {

    private val _stars = mutableListOf<Star>()
    val stars: List<Star> get() = _stars

    fun addStar(star: Star) {
        _stars.add(star)
    }
}