package io.github.takusan23.newradiosupporter.tool

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.*
import io.github.takusan23.newradiosupporter.tool.data.BandData
import io.github.takusan23.newradiosupporter.tool.data.NetworkStatusData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/** ネットワーク関係のコールバック */
object NetworkCallbackTool {

    /** Android 12 以上かどうか */
    private val isAndroidSAndLater: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

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
     * @param context [Context]
     * @param subscriptionId SIMカードを指定する場合は[SubscriptionInfo.getSubscriptionId]を入れる。詳細は[listenMultipleSimNetworkStatus]参照。省略時はデフォルト
     * @return 接続中バンド情報、5Gの種類、5Gの方式 を返す
     */
    fun listenNetworkStatus(context: Context, subscriptionId: Int = SubscriptionManager.DEFAULT_SUBSCRIPTION_ID) = callbackFlow {

        // 一時的に値を持っておく
        var tempRatType: NetworkType? = null
        var tempBandData: BandData? = null
        var nrStandAloneType: NrStandAloneType? = null
        var hasSignalStrengthNr = false
        var simSlotIndex = 0

        /**
         * [TelephonyCallback.CellInfoListener] / [TelephonyCallback.DisplayInfoListener]どっちかが更新されたら呼ぶ
         *
         * Flowに値を返します
         */
        fun sendResult() {
            val bandData = tempBandData
            if (bandData == null) {
                trySend(null)
                return
            }
            val isMmWave = BandDictionary.isMmWave(bandData.earfcn)
            val isLteFrequency = BandDictionary.isLteFrequency(bandData.earfcn)
            val nrType = when {
                // 4Gの場合の処理
                // アンカーバンド か 5Gの電波強度だけ取得できたか
                !bandData.isNR -> when {
                    // SignalStrengthNr が取得できたら 5Gかもしれない 判定
                    // CellInfoNr は取れないけど、電波強度だけ取れた場合は もしかして5G を表示する
                    hasSignalStrengthNr -> FinalNRType.MAYBE_NR
                    // アンカーバンド接続中 なら 4G 判定
                    tempRatType == NetworkType.NR_SUB6 -> FinalNRType.ANCHOR_BAND
                    // 多分 4G
                    else -> FinalNRType.LTE
                }
                // 転用5G
                isLteFrequency -> FinalNRType.NR_LTE_FREQUENCY
                // ミリ波
                isMmWave -> FinalNRType.NR_MMW
                // ミリ波以外 なら Sub6判定
                else -> FinalNRType.NR_SUB6
            }
            trySend(
                NetworkStatusData(
                    simSlotIndex = simSlotIndex,
                    bandData = bandData,
                    finalNRType = nrType,
                    nrStandAloneType = nrStandAloneType ?: NrStandAloneType.ERROR
                )
            )
        }

        if (PermissionCheckTool.isGranted(context)) {
            // SIMカードが選択されている場合は、そのSIMを指定した TelephonyManager を作成する
            // ない場合はデフォルト
            val telephonyManager = (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .createForSubscriptionId(subscriptionId)
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

            // SIMスロット番号を取得する
            @SuppressLint("MissingPermission")
            simSlotIndex = subscriptionManager.getActiveSubscriptionInfo(subscriptionId).simSlotIndex
            // CellInfoIdentity#operatorName が空文字を返すので
            val carrierName = telephonyManager.networkOperatorName

            // セル情報を強制的に更新する
            requestCellInfoUpdate(context, telephonyManager)

            // Android 12より書き方が変わった
            if (isAndroidSAndLater) {
                val callback = object : TelephonyCallback(), TelephonyCallback.DisplayInfoListener, NullableCellInfoListener, TelephonyCallback.SignalStrengthsListener {

                    /** 電波強度 */
                    override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                        // 一部の端末？（Qualcomm Snapdragon）で CellInfoNr が取れない
                        // 取れないが、何故か NR（5G）の電波強度が取れる場合があり（なんで？）、もし取れた場合は 5Gかも知れない を表示している
                        val cellSignalStrengthNr = signalStrength.getCellSignalStrengths(CellSignalStrengthNr::class.java)
                        hasSignalStrengthNr = cellSignalStrengthNr.isNotEmpty()
                        sendResult()
                    }

                    /** 実際の状態 */
                    override fun onCellInfoChanged(cellInfo: MutableList<CellInfo?>?) {
                        // TelephonyCallback#onCellInfoChanged の cellInfo は NonNull だが、端末によっては null を渡すことがある。
                        // Java の場合は NonNull はアノテーションなので静的解析時に null チェックをするため、Java の場合は null を渡して呼び出しても動いていた。
                        // Kotlin の場合は NonNull は実行時にも nullチェックを行うため落ちてしまう。
                        // なので Java で Interface を作成し、TelephonyCallback#onCellInfoChanged を継承し、cellInfo が Nullable になるように修正したものを利用している。
                        if (cellInfo == null) {
                            return
                        }
                        // CellInfoNrを探して、もしない場合は 4G にする
                        // Qualcomm Snapdragon 端末 と Google Tensor 端末 で挙動が違う
                        // Google Tensor の場合は配列の最初に CellInfoNr があるみたい。
                        // Qualcomm Snapdragon の場合、配列のどっかに CellInfoNr があるみたい。
                        // で、 Qualcomm Snapdragon の場合で CellInfoNr が取れない場合がある（ CellSignalStrengthNr だけ取れる。バンドとかは取れないけど5Gの電波強度が取れる？）
                        // ない場合は 4G か アンカーバンド？
                        tempBandData = cellInfo.filterIsInstance<CellInfoNr>().firstOrNull()?.let { convertBandData(it, carrierName) } ?: cellInfo.firstOrNull()?.let { convertBandData(it, carrierName) }
                        sendResult()
                    }

                    /** アンテナピクトと同じやつ。RAT表示とかいう */
                    @SuppressLint("MissingPermission")
                    override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
                        tempRatType = convertNetworkType(telephonyDisplayInfo)
                        nrStandAloneType = convertStandAloneType(telephonyDisplayInfo, telephonyManager.dataNetworkType)
                        sendResult()
                    }
                }
                telephonyManager.registerTelephonyCallback(context.mainExecutor, callback)
                awaitClose { telephonyManager.unregisterTelephonyCallback(callback) }
            } else {
                val callback = object : PhoneStateListener() {
                    @SuppressLint("MissingPermission")
                    override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                        runCatching { super.onSignalStrengthsChanged(signalStrength) }
                        // 一部の端末？（Qualcomm Snapdragon）で CellInfoNr が取れない
                        // 取れないが、何故か NR（5G）の電波強度が取れる場合があり（なんで？）、もし取れた場合は 5Gかも知れない を表示している
                        val cellSignalStrengthNr = signalStrength.getCellSignalStrengths(CellSignalStrengthNr::class.java)
                        hasSignalStrengthNr = cellSignalStrengthNr.isNotEmpty()
                        sendResult()
                    }

                    @SuppressLint("MissingPermission")
                    override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) {
                        // テストコードで super 呼べないため
                        // super、空実装なので呼ぶ必要ない気がするけど一応、、、
                        runCatching { super.onCellInfoChanged(cellInfo) }
                        // 上と同じ
                        tempBandData = cellInfo?.filterIsInstance<CellInfoNr>()?.firstOrNull()?.let { convertBandData(it, carrierName) } ?: cellInfo?.firstOrNull()?.let { convertBandData(it, carrierName) }
                        sendResult()
                    }

                    @SuppressLint("MissingPermission")
                    override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
                        runCatching { super.onDisplayInfoChanged(telephonyDisplayInfo) }
                        tempRatType = convertNetworkType(telephonyDisplayInfo)
                        nrStandAloneType = convertStandAloneType(telephonyDisplayInfo, telephonyManager.dataNetworkType)
                        sendResult()
                    }
                }
                telephonyManager.listen(callback, PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED or PhoneStateListener.LISTEN_CELL_INFO or PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
                awaitClose { telephonyManager.listen(callback, PhoneStateListener.LISTEN_NONE) }
            }
        }
    }

    /**
     * [listenNetworkStatus]の複数SIM対応版。
     * 多分コールバックを監視する必要があるので Flow です
     *
     * SIMの数だけ[listenNetworkStatus]のFlowを返します。SIMカードのスロット順にソートされているはずです。
     */
    @SuppressLint("MissingPermission")
    fun listenMultipleSimNetworkStatus(context: Context) = callbackFlow {
        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        if (PermissionCheckTool.isGranted(context)) {
            // 多分 SubscriptionInfo が更新されたら呼び出される
            val subscriptionInfoCallback = object : SubscriptionManager.OnSubscriptionsChangedListener() {
                override fun onSubscriptionsChanged() {
                    super.onSubscriptionsChanged()
                    trySend(subscriptionManager.activeSubscriptionInfoList.map { listenNetworkStatus(context, it.subscriptionId) })
                }
            }
            subscriptionManager.addOnSubscriptionsChangedListener(context.mainExecutor, subscriptionInfoCallback)
            awaitClose { subscriptionManager.removeOnSubscriptionsChangedListener(subscriptionInfoCallback) }
        } else {
            trySend(emptyList())
            awaitClose {
                // do nothing
            }
        }
    }

    /**
     * データ通信に設定されたSIMカードのスロット番号を取得する。
     *
     * @return データ通信に利用しているSIM。選択されていない場合は null を返す
     */
    @SuppressLint("MissingPermission")
    fun getDataUsageSimSlotIndex(context: Context): Int? {
        val dataUsageSubscriptionId = SubscriptionManager.getActiveDataSubscriptionId()
        // 選択されていない場合
        if (dataUsageSubscriptionId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            return null
        }
        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        return subscriptionManager.getActiveSubscriptionInfo(dataUsageSubscriptionId).simSlotIndex
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

    /** [TelephonyManager.requestCellInfoUpdate]を実行する。最新の状態にする */
    @SuppressLint("MissingPermission")
    private suspend fun requestCellInfoUpdate(context: Context, telephonyManager: TelephonyManager) = suspendCoroutine { continuation ->
        telephonyManager.requestCellInfoUpdate(context.mainExecutor, object : TelephonyManager.CellInfoCallback() {
            override fun onCellInfo(cellInfo: MutableList<CellInfo>) {
                continuation.resume(Unit)
            }

            override fun onError(errorCode: Int, detail: Throwable?) {
                super.onError(errorCode, detail)
                continuation.resume(Unit)
            }
        })
    }

    /**
     * [CellInfo]を簡略化した[BandData]に変換する
     *
     * @param cellInfo [TelephonyCallback.CellInfoListener]で取れるやつ
     * @param carrierName キャリア名。[TelephonyManager.getNetworkOperatorName]
     * @return [BandData]。LTE/NR 以外はnullになります
     * */
    private fun convertBandData(cellInfo: CellInfo, carrierName: String) = when (val cellIdentity = cellInfo.cellIdentity) {
        // LTE
        is CellIdentityLte -> {
            val earfcn = cellIdentity.earfcn
            BandData(
                isNR = false,
                band = BandDictionary.toLTEBand(earfcn),
                earfcn = earfcn,
                carrierName = carrierName,
                frequencyMHz = null,
            )
        }
        // 5G (NR)
        is CellIdentityNr -> {
            val nrarfcn = cellIdentity.nrarfcn
            BandData(
                isNR = true,
                band = BandDictionary.toNRBand(nrarfcn),
                earfcn = nrarfcn,
                carrierName = carrierName,
                frequencyMHz = BandDictionary.toFrequencyMHz(nrarfcn),
            )
        }
        else -> null
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

/**
 * 5Gの状態
 * 4G / 5Gかもしれない / 5G Sub6 / 5G mmWave
 */
enum class FinalNRType {

    /** 実際に5Gのミリ波ネットワークに接続している */
    NR_MMW,

    /** 実際に5GのSub6ネットワークに接続している */
    NR_SUB6,

    /** 実際に転用5Gネットワークに接続している */
    NR_LTE_FREQUENCY,

    /** 5Gの可能性がある。バンド情報は取得できないが、5Gの電波強度だけ取得できたパターン */
    MAYBE_NR,

    /** ピクト表示では5Gだが、実はアンカーバンドの圏内であり、5G接続は利用できないことを示す */
    ANCHOR_BAND,

    /** そもそも4Gだしアンカーバンドの圏内にすらいない */
    LTE,

    /** エラー。準備中など */
    ERROR;

    /** 5G ( SUb-6 / ミリ波 / 転用5G ) の場合は true */
    val isNr: Boolean
        get() = this == NR_SUB6 || this == NR_MMW || this == NR_LTE_FREQUENCY
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