package com.andrerinas.openheadunit.connection.wifi.server

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBuild

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [28])
class WirelessServerNsdTest {
    private class ObservedContext(base: Context) : ContextWrapper(base) {
        var nsdRequests = 0
        override fun getSystemService(name: String): Any? {
            if (name == Context.NSD_SERVICE) {
                nsdRequests++
                return null
            }
            return super.getSystemService(name)
        }
    }

    @Before
    fun reconJet() {
        ShadowBuild.setManufacturer("Recon Instruments")
        ShadowBuild.setModel("JET")
    }

    @Test
    fun `API 16 Jet never creates the crashing platform NSD client`() {
        val context = ObservedContext(RuntimeEnvironment.getApplication())
        assertNull(WirelessServerNsd.manager(context, advertise = true, sdk = 16))
        assertEquals(0, context.nsdRequests)
    }

    @Test
    fun `other API 16 devices still request the platform NSD client`() {
        ShadowBuild.setManufacturer("Example")
        val context = ObservedContext(RuntimeEnvironment.getApplication())
        WirelessServerNsd.manager(context, advertise = true, sdk = 16)
        assertEquals(1, context.nsdRequests)
    }

    @Test
    fun `newer Jet platforms still request the platform NSD client`() {
        val context = ObservedContext(RuntimeEnvironment.getApplication())
        WirelessServerNsd.manager(context, advertise = true, sdk = 17)
        assertEquals(1, context.nsdRequests)
    }

    @Test
    fun `no platform client is created when advertising is not requested`() {
        val context = ObservedContext(RuntimeEnvironment.getApplication())
        assertNull(WirelessServerNsd.manager(context, advertise = false, sdk = 28))
        assertEquals(0, context.nsdRequests)
    }
}
