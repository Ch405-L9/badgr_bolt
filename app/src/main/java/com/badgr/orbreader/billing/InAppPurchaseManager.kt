package com.badgr.orbreader.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InAppPurchaseManager(
    private val context: Context,
    private val onPurchaseSuccess: () -> Unit,
    private val onPurchaseError: (String) -> Unit
) : PurchasesUpdatedListener {

    companion object {
        const val SKU_PRO_MONTHLY  = "badgr_bolt_pro_monthly"
        const val SKU_PRO_LIFETIME = "badgr_bolt_pro_lifetime"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    fun connect() {
        if (billingClient.isReady) {
            _isConnected.value = true
            return
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    _isConnected.value = true
                    scope.launch { queryExistingPurchases() }
                } else {
                    _isConnected.value = false
                    onPurchaseError("Billing setup failed: ${result.debugMessage}")
                }
            }
            override fun onBillingServiceDisconnected() {
                _isConnected.value = false
                scope.launch {
                    delay(3000)
                    connect()
                }
            }
        })
    }

    fun disconnect() {
        scope.cancel()
        billingClient.endConnection()
    }

    private suspend fun queryExistingPurchases() {
        val subResult = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        if (subResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            handlePurchaseList(subResult.purchasesList)
        }
        val inappResult = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        if (inappResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            handlePurchaseList(inappResult.purchasesList)
        }
    }

    fun launchSubscriptionFlow(activity: Activity) {
        launchFlow(activity, SKU_PRO_MONTHLY, BillingClient.ProductType.SUBS)
    }

    fun launchLifetimeFlow(activity: Activity) {
        launchFlow(activity, SKU_PRO_LIFETIME, BillingClient.ProductType.INAPP)
    }

    private fun launchFlow(activity: Activity, productId: String, productType: String) {
        if (billingClient.isReady.not()) {
            onPurchaseError("Billing client not ready. Please try again.")
            return
        }
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(productType)
                .build()
        )
        scope.launch {
            val result = billingClient.queryProductDetails(
                QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build()
            )
            if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK
                || result.productDetailsList.isNullOrEmpty()) {
                withContext(Dispatchers.Main) {
                    onPurchaseError("Product not found: $productId")
                }
                return@launch
            }
            val productDetails = result.productDetailsList!!.first()
            val productDetailsParamsList = if (productType == BillingClient.ProductType.SUBS) {
                val offerToken = productDetails.subscriptionOfferDetails
                    ?.firstOrNull()?.offerToken ?: run {
                    withContext(Dispatchers.Main) {
                        onPurchaseError("No subscription offer found.")
                    }
                    return@launch
                }
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )
            } else {
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            }
            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()
            withContext(Dispatchers.Main) {
                billingClient.launchBillingFlow(activity, flowParams)
            }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.let { handlePurchaseList(it) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> { }
            else -> {
                onPurchaseError("Purchase failed: ${result.debugMessage}")
            }
        }
    }

    private fun handlePurchaseList(purchases: List<Purchase>) {
        scope.launch {
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (purchase.isAcknowledged.not()) {
                        acknowledgePurchase(purchase)
                    }
                    withContext(Dispatchers.Main) {
                        _isPro.value = true
                        onPurchaseSuccess()
                    }
                }
            }
        }
    }

    private suspend fun acknowledgePurchase(purchase: Purchase) {
        val result = billingClient.acknowledgePurchase(
            AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        )
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            withContext(Dispatchers.Main) {
                onPurchaseError("Acknowledgement failed: ${result.debugMessage}")
            }
        }
    }
}
