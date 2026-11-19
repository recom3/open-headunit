package com.andrerinas.openheadunit.utils

import android.os.Build

object ReconJetDevice {
    fun isReconJet(): Boolean =
        Build.MANUFACTURER.equals("Recon Instruments", ignoreCase = true) &&
            Build.MODEL.equals("JET", ignoreCase = true)
}
