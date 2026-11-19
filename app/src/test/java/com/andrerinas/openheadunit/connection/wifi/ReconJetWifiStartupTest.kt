package com.andrerinas.openheadunit.connection.wifi

import android.app.Application
import android.content.Context
import android.net.wifi.WifiManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBuild

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [28])
class ReconJetWifiStartupTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val wifi get() = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    @Before
    @Suppress("DEPRECATION")
    fun jetWithWifiOff() {
        ShadowBuild.setManufacturer("Recon Instruments")
        ShadowBuild.setModel("JET")
        wifi.setWifiEnabled(false)
    }

    @Test
    fun `automatic Jet server startup enables a disabled radio`() {
        assertTrue(start())
        assertTrue(wifi.isWifiEnabled)
    }

    @Test
    @Suppress("DEPRECATION")
    fun `already enabled radio is left alone`() {
        wifi.setWifiEnabled(true)
        assertFalse(start())
        assertTrue(wifi.isWifiEnabled)
    }

    @Test
    fun `ordinary device keeps its disabled radio`() {
        ShadowBuild.setManufacturer("Example")
        assertFalse(start())
        assertFalse(wifi.isWifiEnabled)
    }

    @Test
    fun `manual and native modes do not enable the radio`() {
        for (mode in listOf(WifiLauncherMode.MANUAL, WifiLauncherMode.NATIVE, WifiLauncherMode.HELPER)) {
            assertFalse(start(mode = mode))
            assertFalse(wifi.isWifiEnabled)
        }
    }

    @Test
    fun `reconnect opt out keeps the radio disabled`() {
        assertFalse(start(reconnect = false))
        assertFalse(wifi.isWifiEnabled)
    }

    @Test
    fun `wireless selection opt out keeps the radio disabled`() {
        assertFalse(start(wireless = false))
        assertFalse(wifi.isWifiEnabled)
    }

    private fun start(
        mode: WifiLauncherMode = WifiLauncherMode.AUTO,
        reconnect: Boolean = true,
        wireless: Boolean = true,
    ) = ReconJetWifiStartup.enableForAutomaticServer(context, mode, reconnect, wireless)
}
