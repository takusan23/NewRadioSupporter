package io.github.takusan23.newradiosupporter.tool

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.*
import io.github.takusan23.newradiosupporter.tool.data.BandData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

/** ネットワーク関係のコールバック */
object NetworkCallbackTool {

    /**
     * 定額制、従量制をコールバックで受け取れるらしいんだけど動いてなくね？
     *
     * @return 無制限プラン、もしくは家のWi-Fi等定額制ネットワークの場合はtrueを返す
     * */
    fun listenUnlimitedNetwork(context: Context) = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                // 無制限プランを契約している場合はtrue
                val isUnlimited = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) ||
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_TEMPORARILY_NOT_METERED)
                trySend(isUnlimited)
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }

    /**
     * 4G / アンカーバンド / 5G-Sub6 / 5G-ミリ波 をFlowで監視する関数
     *
     * アンカーバンドではなく、本当に5G-Sub6に接続できた場合は[TelephonyCallback.CellInfoListener]で[CellInfoNr]が取得できます。
     *
     * が、アンテナピクトの隣が5Gでもアンカーバンドに接続している場合は[CellInfoLte]を返します。
     *
     * じゃあアンテナピクトの隣の状態はどこで拾えるのかって話ですが、[TelephonyCallback.DisplayInfoListener]で取れます。
     *
     * 他にもLTEのキャリアアグリゲーションとか取れるやつです。これとさっきの[CellInfo]を見比べて同じじゃない場合はアンカーバンドであるということがわかります。
     *
     * ミリ波かSub6かどうかは、NRARFCNの値を比較することで判断できます。[BandDictionary.isMmWave]参照
     *
     * 5G接続時は、ノンスタンドアローンかスタンドアローンかどうかも返します。
     *
     * [BandData]と[NetworkType]と[NrStandAloneType]の[Triple]をFlowで流します
     *
     * @param context [Context]
     * @return 接続中バンド情報、5Gの種類、5Gの方式 を返す
     * */
    fun listenNetworkStatus(context: Context) = callbackFlow {

        // 一時的に値を持っておく
        var tempNetworkType: NetworkType? = null
        var tempBandData: BandData? = null
        var nrStandAloneType: NrStandAloneType? = null

        /**
         * [TelephonyCallback.CellInfoListener] / [TelephonyCallback.DisplayInfoListener]どっちかが更新されたら呼ぶ
         *
         * Flowに値を返します
         * */
        fun sendResult() {
            if (tempBandData == null) {
                trySend(Triple(tempBandData, FinalNRType.ERROR, NrStandAloneType.ERROR))
                return
            }
            val isMmWave = BandDictionary.isMmWave(tempBandData!!.earfcn)
            val result = when {
                // Sub6かアンカーバンド接続中 かつ 5Gではない なら 4G 判定
                tempNetworkType == NetworkType.NR_SUB6 && !tempBandData!!.isNR -> FinalNRType.ANCHOR_BAND
                // Sub6かアンカーバンド接続中 かつ 5G なら Sub6判定
                tempBandData!!.isNR && !isMmWave -> FinalNRType.NR_SUB6
                // ミリ波
                tempBandData!!.isNR && isMmWave -> FinalNRType.NR_MMW
                // そもそも4G
                else -> FinalNRType.LTE
            }
            trySend(Triple(tempBandData, result, nrStandAloneType))
        }

        if (PermissionCheckTool.isGranted(context)) {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            // Android 12より書き方が変わった
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val callback = object : TelephonyCallback(), TelephonyCallback.DisplayInfoListener, TelephonyCallback.CellInfoListener {
                    /** 実際の状態 */
                    override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>) {
                        tempBandData = convertBandData(cellInfo[0])
                        sendResult()
                    }

                    /** アンテナピクトと同じやつ */
                    @SuppressLint("MissingPermission")
                    override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
                        tempNetworkType = convertNetworkType(telephonyDisplayInfo)
                        nrStandAloneType = convertStandAloneType(telephonyDisplayInfo, telephonyManager.dataNetworkType)
                        sendResult()
                    }
                }
                telephonyManager.registerTelephonyCallback(context.mainExecutor, callback)
                awaitClose { telephonyManager.unregisterTelephonyCallback(callback) }
            } else {
                val callback = object : PhoneStateListener() {

                    @SuppressLint("MissingPermission")
                    override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) {
                        super.onCellInfoChanged(cellInfo)
                        tempBandData = cellInfo?.get(0)?.let { convertBandData(it) }
                        sendResult()
                    }

                    @SuppressLint("MissingPermission")
                    override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
                        super.onDisplayInfoChanged(telephonyDisplayInfo)
                        tempNetworkType = convertNetworkType(telephonyDisplayInfo)
                        nrStandAloneType = convertStandAloneType(telephonyDisplayInfo, telephonyManager.dataNetworkType)
                        sendResult()
                    }
                }
                telephonyManager.listen(callback, PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED or PhoneStateListener.LISTEN_CELL_INFO)
                awaitClose { telephonyManager.listen(callback, PhoneStateListener.LISTEN_NONE) }
            }
        }
    }

    /**
     * [CellInfo]を簡略化した[BandData]に変換する
     *
     * @param cellInfo [TelephonyCallback.CellInfoListener]で取れるやつ
     * @return [BandData]。LTE/NR 以外はnullになります
     * */
    private fun convertBandData(cellInfo: CellInfo) = when (cellInfo) {
        // LTE
        is CellInfoLte -> {
            val earfcn = cellInfo.cellIdentity.earfcn
            BandData(false, BandDictionary.toLTEBand(earfcn), earfcn, cellInfo.cellIdentity.operatorAlphaShort.toString())
        }
        // 5G (NR)
        is CellInfoNr -> {
            val nrarfcn = (cellInfo.cellIdentity as CellIdentityNr).nrarfcn
            BandData(true, BandDictionary.toNRBand(nrarfcn), nrarfcn, cellInfo.cellIdentity.operatorAlphaShort.toString())
        }
        else -> null
    }

    /**
     * [TelephonyDisplayInfo]から簡略化した[NetworkType]を返す
     *
     * @param telephonyDisplayInfo [TelephonyCallback.DisplayInfoListener]で取れるやつ
     * @return [NetworkType]。LTE/NR 以外は [NetworkType.NONE]
     * */
    fun convertNetworkType(telephonyDisplayInfo: TelephonyDisplayInfo) = when (telephonyDisplayInfo.overrideNetworkType) {
        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_ADVANCED_PRO -> NetworkType.LTE_ADVANCED
        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_CA -> NetworkType.LTE_CA
        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA -> NetworkType.NR_SUB6
        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED -> NetworkType.NR_MMW
        else -> NetworkType.NONE
    }

    /**
     * [TelephonyDisplayInfo]と[TelephonyManager.getDataNetworkType]を見て5Gが NSA/SA のどっちで接続されているか判別する
     *
     * @param telephonyDisplayInfo [TelephonyCallback.DisplayInfoListener]で取れるやつ
     * @param dataNetworkType [TelephonyManager.getDataNetworkType]の値
     * @return [NrStandAloneType]。5G 以外は [NrStandAloneType.ERROR]
     */
    fun convertStandAloneType(telephonyDisplayInfo: TelephonyDisplayInfo, dataNetworkType: Int) = when {
        /**
         * 5G スタンドアローン形式 (SA)
         * [TelephonyManager.getDataNetworkType]が[TelephonyManager.NETWORK_TYPE_NR]を返す
         */
        dataNetworkType == TelephonyManager.NETWORK_TYPE_NR -> NrStandAloneType.STAND_ALONE
        /**
         * 5G ノンスタンドアローン方式 (NSA)
         * [TelephonyManager.getDataNetworkType]が[TelephonyManager.NETWORK_TYPE_LTE]を返し（なんと！）、
         * [TelephonyDisplayInfo.getOverrideNetworkType]が NR を返す
         */
        dataNetworkType == TelephonyManager.NETWORK_TYPE_LTE &&
                (telephonyDisplayInfo.overrideNetworkType == TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED
                        || telephonyDisplayInfo.overrideNetworkType == TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA) -> NrStandAloneType.NON_STAND_ALONE
        // 5Gじゃない
        else -> NrStandAloneType.ERROR
    }
}

/** ネットワークの種類 */
enum class NetworkType {
    /** 特になし。getNetworkType()を利用してね */
    NONE,

    /** LTE-Advancedが有効（CAと何が違うの？） */
    LTE_ADVANCED,

    /** キャリアアグリゲーションが有効 */
    LTE_CA,

    /** 5GのSub6ネットワークか、アンカーバンド圏内の場合 */
    NR_SUB6,

    /** ミリ波 ネットワーク もしくは キャリアアグリゲーション（CA）等より速い手段が提供されている場合 */
    NR_MMW,
}

/** 4G / EN-DC / 5G Sub6 / 5G mmWave */
enum class FinalNRType {
    /** ピクト表示では5Gだが、実はアンカーバンドの圏内であり、5G接続は利用できないことを示す */
    ANCHOR_BAND,

    /** 実際に5GのSub6ネットワークに接続している */
    NR_SUB6,

    /** 実際に5Gのミリ波ネットワークに接続している */
    NR_MMW,

    /** そもそも4Gだしアンカーバンドの圏内にすらいない */
    LTE,

    /** エラー。準備中など */
    ERROR
}

/**
 * 5Gのネットワーク方式、動作未確認
 * Non StandAlone / StandAlone / 5G以外
 */
enum class NrStandAloneType {
    /** 5G スタンドアローン形式 */
    STAND_ALONE,

    /** 5G ノンスタンドアローン形式 */
    NON_STAND_ALONE,

    /** 5G じゃない */
    ERROR
}