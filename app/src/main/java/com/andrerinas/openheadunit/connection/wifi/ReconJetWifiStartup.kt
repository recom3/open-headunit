package com.andrerinas.openheadunit.connection.wifi

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import com.andrerinas.openheadunit.utils.ReconJetDevice

/** One startup request, not a retry loop: Recon's charging screen can leave Wi-Fi off. */
object ReconJetWifiStartup {
    @Suppress("DEPRECATION")
    fun enableForAutomaticServer(
        context: Context,
        mode: WifiLauncherMode,
        reconnectLastSession: Boolean,
        wirelessSelected: Boolean,
    ): Boolean {
        if (!ReconJetDevice.isReconJet() || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
            mode != WifiLauncherMode.AUTO || !reconnectLastSession || !wirelessSelected
        ) return false

        val manager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            ?: return false
        if (manager.isWifiEnabled || manager.wifiState == WifiManager.WIFI_STATE_ENABLING) return false
        return manager.setWifiEnabled(true)
    }
}
