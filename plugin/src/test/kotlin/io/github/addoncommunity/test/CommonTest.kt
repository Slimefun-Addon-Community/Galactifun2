package io.github.addoncommunity.test

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.UnimplementedOperationException
import com.github.shynixn.mccoroutine.bukkit.MCCoroutine
import com.github.shynixn.mccoroutine.bukkit.test.TestMCCoroutine
import io.github.addoncommunity.galactifun.Galactifun2
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class CommonTest {

    private lateinit var driver: String

    @BeforeEach
    fun setUp() {
        MockBukkit.mock()
        MockBukkit.load(Slimefun::class.java)
        driver = MCCoroutine.Driver
        MCCoroutine.Driver = TestMCCoroutine.Driver
        try {
            MockBukkit.load(Galactifun2::class.java)
        } catch (e: UnimplementedOperationException) {
            e.printStackTrace()
        }
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
        MCCoroutine.Driver = driver
    }
}