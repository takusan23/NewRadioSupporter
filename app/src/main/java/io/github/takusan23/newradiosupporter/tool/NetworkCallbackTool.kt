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
     * @return [NetworkType]を返す
     * */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun listenNetworkStatus(context: Context) = callbackFlow {
        if (PermissionCheckTool.isGranted(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            /** Flowへ値を流す */
            fun parse(telephonyDisplayInfo: TelephonyDisplayInfo) {
                when (telephonyDisplayInfo.overrideNetworkType) {
                    TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_ADVANCED_PRO -> trySend(NetworkType.LTE_ADVANCED)
                    TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_CA -> trySend(NetworkType.LTE_CA)
                    TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA -> trySend(NetworkType.NR_SUB6)
                    TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED -> trySend(NetworkType.NR_MMW)
                    else -> trySend(NetworkType.NONE)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val callback = object : TelephonyCallback(), TelephonyCallback.DisplayInfoListener {
                    override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
                        parse(telephonyDisplayInfo)
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
                        parse(telephonyDisplayInfo)
                    }
                }
                telephonyManager.listen(callback, PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED)
                awaitClose { telephonyManager.listen(callback, PhoneStateListener.LISTEN_NONE) }
            }
        }
    }

    /** 両方の結果から最終的な答え[FinalNRType]を出す */
    fun finalResult(bandData: BandData, networkType: NetworkType): FinalNRType {
        return when {
            // Sub6かアンカーLTEバンド接続中 かつ 5Gではない なら 4G 判定
            networkType == NetworkType.NR_SUB6 && !bandData.isNR -> FinalNRType.ANCHOR_LTE_BAND
            // Sub6かアンカーLTEバンド接続中 かつ 5G なら Sub6判定
            networkType == NetworkType.NR_SUB6 && bandData.isNR -> FinalNRType.NR_SUB6
            // ミリ波はスタンドアロンなのでEN-DCの影響を受けない
            networkType == NetworkType.NR_MMW -> FinalNRType.NR_MMW
            // そもそも4G
            else -> FinalNRType.LTE
        }
    }

}

/** 最終的な値を取得するには[NetworkCallback.finalResult]を利用してね */
enum class NetworkType {
    /** 特になし。getNetworkType()を利用してね */
    NONE,

    /** LTE-Advancedが有効（CAと何が違うの？） */
    LTE_ADVANCED,

    /** キャリアアグリゲーションが有効 */
    LTE_CA,

    /** 5GのSub6ネットワークか、アンカーLTEバンド圏内の場合 */
    NR_SUB6,

    /** ミリ波 ネットワーク もしくは キャリアアグリゲーション（CA）等より速い手段が提供されている場合 */
    NR_MMW,
}


enum class FinalNRType {
    /** ピクト表示では5Gだが、実はアンカーLTEバンドの圏内であり、5G接続は利用できないことを示す */
    ANCHOR_LTE_BAND,

    /** 実際に5GのSub6ネットワークに接続している */
    NR_SUB6,

    /** 実際に5Gのミリ波ネットワークに接続している */
    NR_MMW,

    /** そもそも4GだしアンカーLTEバンドの圏内にすらいない */
    LTE,
}