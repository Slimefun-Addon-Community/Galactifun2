package io.github.addoncommunity.galactifun.math

import io.github.addoncommunity.galactifun.api.objects.planet.lambertsUniversalVariables
import org.joml.Vector2d
import kotlin.test.Test

class LambertTest {

    @Test
    fun testLambert() {
        val deltaT = 76L

        val r1 = Vector2d(15945.34, 0.0)
        val r2 = Vector2d(12214.83399, 10249.46731)

        val (v1, v2) = lambertsUniversalVariables(r1, r2, deltaT, 1.32712440018e20, 1)!!
        println("%.5f %.5f".format(v1.x, v1.y))
        println("%.5f %.5f".format(v2.x, v2.y))
    }
}