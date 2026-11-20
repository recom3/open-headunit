package com.andrerinas.openheadunit.aap

import android.app.UiModeManager
import android.content.Context
import com.andrerinas.openheadunit.utils.ReconJetDevice

/** Owns the optional Android car UI mode, independently of the projection service. */
object CarModeController {
    fun setup(context: Context, carLauncherActive: Boolean): UiModeManager? {
        val reconJet = ReconJetDevice.isReconJet()
        if (carLauncherActive && !reconJet) return null

        val manager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
            ?: return null
        if (reconJet) {
            // Recon Message Center repeatedly raises Android's ongoing car-mode notification.
            // Also clear a mode left enabled by an older OHU build; projection does not need it.
            manager.disableCarMode(0)
            return null
        }

        manager.enableCarMode(0)
        return manager
    }
}
