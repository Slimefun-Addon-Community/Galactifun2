package io.github.addoncommunity.test

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.UnimplementedOperationException
import io.github.addoncommunity.galactifun.Galactifun2
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class CommonTest {

    @BeforeEach
    fun setUp() {
        MockBukkit.mock()
        MockBukkit.load(Slimefun::class.java)
        try {
            MockBukkit.load(Galactifun2::class.java)
        } catch (e: UnimplementedOperationException) {
            e.printStackTrace()
        }
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }
}