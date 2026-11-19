package com.andrerinas.openheadunit.aap

import android.app.Application
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBuild

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [28])
class CarModeControllerTest {
    private val context get() = RuntimeEnvironment.getApplication<Application>()
    private val manager get() = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager

    @Before
    fun ordinaryHeadUnit() {
        ShadowBuild.setManufacturer("Example")
        ShadowBuild.setModel("Head unit")
        manager.disableCarMode(0)
    }

    @Test
    fun `Jet projection does not enter system car mode`() {
        jet()
        assertNull(CarModeController.setup(context, carLauncherActive = false))
        assertEquals(Configuration.UI_MODE_TYPE_NORMAL, manager.currentModeType)
    }

    @Test
    fun `Jet clears old car mode without changing night mode`() {
        jet()
        manager.nightMode = UiModeManager.MODE_NIGHT_YES
        manager.enableCarMode(0)
        assertEquals(Configuration.UI_MODE_TYPE_CAR, manager.currentModeType)
        assertNull(CarModeController.setup(context, carLauncherActive = false))
        assertEquals(Configuration.UI_MODE_TYPE_NORMAL, manager.currentModeType)
        assertEquals(UiModeManager.MODE_NIGHT_YES, manager.nightMode)
    }

    @Test
    fun `ordinary head unit still enters and owns car mode`() {
        assertSame(manager, CarModeController.setup(context, carLauncherActive = false))
        assertEquals(Configuration.UI_MODE_TYPE_CAR, manager.currentModeType)
    }

    @Test
    fun `ordinary car launcher continues to skip system car mode`() {
        assertNull(CarModeController.setup(context, carLauncherActive = true))
        assertEquals(Configuration.UI_MODE_TYPE_NORMAL, manager.currentModeType)
    }

    @Test
    fun `another Recon model keeps ordinary car mode behaviour`() {
        ShadowBuild.setManufacturer("Recon Instruments")
        ShadowBuild.setModel("SNOW2")
        assertSame(manager, CarModeController.setup(context, carLauncherActive = false))
        assertEquals(Configuration.UI_MODE_TYPE_CAR, manager.currentModeType)
    }

    @Test
    fun `Jet model name from another manufacturer keeps ordinary behaviour`() {
        ShadowBuild.setModel("JET")
        assertSame(manager, CarModeController.setup(context, carLauncherActive = false))
        assertEquals(Configuration.UI_MODE_TYPE_CAR, manager.currentModeType)
    }

    private fun jet() {
        ShadowBuild.setManufacturer("Recon Instruments")
        ShadowBuild.setModel("JET")
    }
}
