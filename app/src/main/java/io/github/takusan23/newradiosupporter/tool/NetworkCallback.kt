package io.github.takusan23.newradiosupporter.tool

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

/** ネットワーク関係のコールバック */
object NetworkCallback {

    /**
     * [BandTool.getBandDataFromEarfcnOrNrafcn]をコールバックで定期的に受け取る
     *
     * @return [BandTool.getBandDataFromEarfcnOrNrafcn]参照
     * */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun listenBand(context: Context) = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                launch { trySend(BandTool.getBandDataFromEarfcnOrNrafcn(context)) }
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }

    /**
     * [BandTool.getBandDataFromEarfcnOrNrafcn]をコールバックで定期的に受け取る
     *
     * @return 無制限プラン、もしくは家のWi-Fi等定額制ネットワークの場合はtrueを返す
     * */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun listenUnlimitedNetwork(context: Context) = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                // 無制限プランを契約している場合はtrue
                val isUnlimited = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) ||
                            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_TEMPORARILY_NOT_METERED)
                } else {
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                }
                trySend(isUnlimited)
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }

    /**
     * 5G(New Radio / NR) 関係のコールバックを受け取る。多分Android 11以上じゃないと無理です
     *
     * @return [NRType]を返す
     * */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun listenNewRadioStatus(context: Context) = callbackFlow {
        if (PermissionCheckTool.isGranted(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val callback = object : TelephonyCallback(), TelephonyCallback.DisplayInfoListener {
                    override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
                        when (telephonyDisplayInfo.overrideNetworkType) {
                            TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA -> trySend(NRType.NR_SUB6)
                            TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED -> trySend(NRType.NR_MW)
                        }
                    }
                }
                telephonyManager.registerTelephonyCallback(context.mainExecutor, callback)
                awaitClose { telephonyManager.unregisterTelephonyCallback(callback) }
            } else {
                val callback = object : PhoneStateListener() {
                    // パーミッションチェック入れてるので黙らせる
                    @SuppressLint("MissingPermission", "NewApi")
                    override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
                        super.onDisplayInfoChanged(telephonyDisplayInfo)
                        when (telephonyDisplayInfo.overrideNetworkType) {
                            TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA -> trySend(NRType.NR_SUB6)
                            TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED -> trySend(NRType.NR_MW)
                        }
                    }
                }
                telephonyManager.listen(callback, PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED)
                awaitClose { telephonyManager.listen(callback, PhoneStateListener.LISTEN_NONE) }
            }
        }
    }

}

enum class NRType {
    /** Sub 6 ネットワーク */
    NR_SUB6,

    /** ミリ波 ネットワーク もしくは キャリアアグリゲーション（CA）等より速い手段が提供されている場合 */
    NR_MW,
}