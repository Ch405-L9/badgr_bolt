package com.badgr.orbreader

import android.app.Application
import com.badgr.orbreader.billing.InAppPurchaseManager

/**
 * Application subclass – declared in AndroidManifest.xml via android:name=".OrbReaderApp".
 * Initialises global singletons: billing client.
 */
class OrbReaderApp : Application() {

    lateinit var purchaseManager: InAppPurchaseManager
        private set

    override fun onCreate() {
        super.onCreate()
        purchaseManager = InAppPurchaseManager(
            context = this,
            onPurchaseSuccess = {
                // ProGate will observe isPro StateFlow — no action needed here
            },
            onPurchaseError = { error ->
                android.util.Log.e("InAppPurchase", "Purchase error: $error")
            }
        )
        purchaseManager.connect()
    }

    override fun onTerminate() {
        super.onTerminate()
        purchaseManager.disconnect()
    }
}
