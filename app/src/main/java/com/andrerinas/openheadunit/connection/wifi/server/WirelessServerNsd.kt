package com.andrerinas.openheadunit.connection.wifi.server

import android.content.Context
import android.net.nsd.NsdManager
import android.os.Build
import com.andrerinas.openheadunit.utils.ReconJetDevice

object WirelessServerNsd {
    fun manager(context: Context, advertise: Boolean, sdk: Int = Build.VERSION.SDK_INT): NsdManager? {
        // The API-16 Jet's NsdService can dereference a missing ClientInfo at boot and kill
        // system_server. Do not even create its asynchronous client on that platform.
        // Head Unit Server discovery already has a direct TCP gateway/subnet path.
        if (!advertise || (sdk == 16 && ReconJetDevice.isReconJet())) return null
        return context.getSystemService(Context.NSD_SERVICE) as? NsdManager
    }
}
