package io.github.takusan23.newradiosupporter.tool

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.CellIdentityLte
import android.telephony.CellIdentityNr
import android.telephony.CellInfo
import android.telephony.CellInfoNr
import android.telephony.CellSignalStrengthNr
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyCallback
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import io.github.takusan23.newradiosupporter.tool.NetworkStatusFlow.collectMultipleSimSubscriptionIdList
import io.github.takusan23.newradiosupporter.tool.NetworkStatusFlow.collectNetworkStatus
import io.github.takusan23.newradiosupporter.tool.data.BandData
import io.github.takusan23.newradiosupporter.tool.data.FinalNrType
import io.github.takusan23.newradiosupporter.tool.data.NetworkStatusData
import io.github.takusan23.newradiosupporter.tool.data.NrStandAloneType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object NetworkStatusFlow {

    /** Android 12 以上かどうか */
    private val isAndroidSAndLater: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    /** [collectNetworkStatus]の間隔 */
    private const val DELAY_MS = 3_000L


    /**
     * [collectMultipleSimSubscriptionIdList]と[collectNetworkStatus]の合体版
     *
     * @param context [Context]
     * @return SIM カードの枚数分 [NetworkStatusData] を返す [Flow]。SIM カードが変化しても購読できる。
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun collectMultipleNetworkStatus(context: Context) = collectMultipleSimSubscriptionIdList(context)
        // transformLatest を使い、Flow から新しい値が流れてきた場合はこのブロックのコルーチンをキャンセルして再起動する
        // これで SIM カードの状態が変化しても、電測データを再度購読し直すようにしている
        .transformLatest { subscriptionIdList ->
            // 配列を返したい
            val statusArray = Array<NetworkStatusData?>(subscriptionIdList.size) { null }

            subscriptionIdList
                // SIM カードの枚数分 電測データを購読する
                .mapIndexed { index, subscriptionId ->
                    collectNetworkStatus(
                        context = context,
                        subscriptionId = subscriptionId
                    ).onEach { networkStatusData ->
                        // 返り値は配列にしたい
                        statusArray[index] = networkStatusData
                        // 値が来たら transformLatest に渡すことで、この Flow の返り値となる
                        emit(statusArray.filterNotNull())
                    }
                }
                // 複数の Flow を1つにして、購読する
                .merge()
                .collect()
        }

    /**
     * 定額制、従量制をコールバックで受け取れるらしいんだけど動いてなくね？
     *
     * @return 無制限プラン、もしくは家のWi-Fi等定額制ネットワークの場合はtrueを返す
     */
    fun collectUnlimitedNetwork(context: Context) = callbackFlow {
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
     * データ通信に設定された SIM カードの SubscriptionId を取得する
     *
     * @return データ通信に利用している SubscriptionId。選択されていない場合は null を返す
     */
    @SuppressLint("MissingPermission")
    fun getDataUsageSubscriptionId(): Int? {
        val dataUsageSubscriptionId = SubscriptionManager.getActiveDataSubscriptionId()
        // 選択されていない場合
        if (dataUsageSubscriptionId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            return null
        }
        return dataUsageSubscriptionId
    }

    /**
     * [NetworkStatusData]を取得する。
     * 取得タイミングは [TelephonyManager]のコールバック + 一定間隔で同期APIを叩く です。
     * コールバックAPIのみでも良いはずですが、なんか調子悪いので追加で同期APIも一定間隔で叩くようにしてみました。
     *
     * @param context [Context]
     * @param subscriptionId SIMカードを指定する場合は[SubscriptionInfo.getSubscriptionId]を入れる。狙ったSIMカードの回線状況が取得できます。
     */
    @SuppressLint("MissingPermission")
    private fun collectNetworkStatus(
        context: Context,
        subscriptionId: Int = SubscriptionManager.DEFAULT_SUBSCRIPTION_ID
    ) = callbackFlow {

        // SIMカードが選択されている場合は、そのSIMを指定した TelephonyManager を作成する
        // ない場合はデフォルト
        val telephonyManager = (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .createForSubscriptionId(subscriptionId)
        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

        // SIM カード情報
        // 一部の端末で null になるっぽい？
        val subscriptionInfo: SubscriptionInfo? = subscriptionManager.getActiveSubscriptionInfo(subscriptionId)
        val simInfo = when {
            subscriptionInfo?.isEmbedded == true -> NetworkStatusData.SimInfo.Esim(subscriptionId)
            else -> NetworkStatusData.SimInfo.PhysicalSim(subscriptionId, simSlotIndex = subscriptionInfo?.simSlotIndex ?: 0)
        }
        // キャリア名
        val carrierName = telephonyManager.networkOperatorName
        // 通信キャリアの MCC / MNC を取得する
        // MCC 3桁 + MNC 2桁 で構成されているはず
        // SIM カードが未挿入の場合は空文字を返す
        val (mcc, mnc) = telephonyManager.networkOperator
            .let { plmn -> plmn.take(3) to plmn.takeLast(2) }
        // TelephonyDisplayInfoはコールバックのみの提供なので
        var tempTelephonyDisplayInfo: TelephonyDisplayInfo? = null

        /**
         * 同期APIを叩く
         * @return [NetworkStatusData]
         */
        suspend fun getNetworkStatusData() = runCatching {
            // もしかして5G の判定を行う
            // 一部の端末？（Qualcomm Snapdragon）で CellInfoNr が取れない
            // 取れないが、何故か NR（5G）の電波強度が取れる場合があり（なんで？）、もし取れた場合は 5Gかも知れない を表示している
            val hasSignalStrengthNr = telephonyManager.signalStrength
                ?.getCellSignalStrengths(CellSignalStrengthNr::class.java)
                ?.isNotEmpty() == true

            // アンテナピクトと同じやつ。RAT表示とかいう
            // これだけはコールバック版を見るしか無い
            val ratType = tempTelephonyDisplayInfo?.let { convertNetworkType(it) } ?: NetworkType.NONE
            val nrStandAloneType = tempTelephonyDisplayInfo?.let { convertStandAloneType(it, telephonyManager.dataNetworkType) } ?: NrStandAloneType.ERROR

            // CellInfo の取得を行う
            // 接続中の NRARFCN とかを取得するために必要
            val cellInfoList = awaitRequestCellInfoUpdate(context, telephonyManager)
            // CellInfoNrを探して、もしない場合は 4G にする
            // Qualcomm Snapdragon 端末 と Google Tensor 端末 で挙動が違う
            // Google Tensor の場合は配列の最初に CellInfoNr があるみたい。
            // Qualcomm Snapdragon の場合、配列のどっかに CellInfoNr があるみたい。
            // で、 Qualcomm Snapdragon の場合で CellInfoNr が取れない場合がある（ CellSignalStrengthNr だけ取れる。バンドとかは取れないけど5Gの電波強度が取れる？）
            // ない場合は 4G か アンカーバンド？
            val cellInfo = cellInfoList.filterIsInstance<CellInfoNr>().firstOrNull() ?: cellInfoList.firstOrNull()
            val bandData = cellInfo?.let { convertBandData(mcc, mnc, it, carrierName) } ?: return@runCatching null // BandData 取れない場合は何もできないので return してしまう

            val nrType = when {
                // 4Gの場合の処理
                // アンカーバンド か 5Gの電波強度だけ取得できたか
                !bandData.isNR -> when {
                    // SignalStrengthNr が取得できたら 5Gかもしれない 判定
                    // CellInfoNr は取れないけど、電波強度だけ取れた場合は もしかして5G を表示する
                    hasSignalStrengthNr -> FinalNrType.MAYBE_NR
                    // アンカーバンド接続中 なら 4G 判定
                    ratType == NetworkType.NR_SUB6 -> FinalNrType.ANCHOR_BAND
                    // 多分 4G
                    else -> FinalNrType.LTE
                }
                // 転用5G
                BandDictionaryTool.isLteFrequency(bandData.earfcn) -> FinalNrType.NR_LTE_FREQUENCY
                // ミリ波
                BandDictionaryTool.isMmWave(bandData.earfcn) -> FinalNrType.NR_MMW
                // ミリ波以外 なら Sub6判定
                else -> FinalNrType.NR_SUB6
            }

            NetworkStatusData(simInfo, bandData, nrType, nrStandAloneType)
        }.onFailure {
            // runCatching 内での失敗を出力
            it.printStackTrace()
        }.getOrNull()

        // 本当は非同期のコールバックAPIだけで良いはずだが、
        // うまく更新されないことがあり（CellInfoNr があるのに もしかして5G のまま等）
        // 一定間隔で同期APIも叩くようにしている。
        launch {
            // while でブロックしてしまうので 子コルーチン
            while (isActive) {
                val statusData = getNetworkStatusData()
                trySend(statusData)
                delay(DELAY_MS)
            }
        }

        if (isAndroidSAndLater) {
            val callback = object : TelephonyCallback(), TelephonyCallback.DisplayInfoListener, NullableCellInfoListener, TelephonyCallback.SignalStrengthsListener {
                override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                    launch { trySend(getNetworkStatusData()) }
                }

                override fun onCellInfoChanged(cellInfo: MutableList<CellInfo?>?) {
                    launch { trySend(getNetworkStatusData()) }
                }

                override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
                    tempTelephonyDisplayInfo = telephonyDisplayInfo
                    launch { trySend(getNetworkStatusData()) }
                }
            }
            telephonyManager.registerTelephonyCallback(context.mainExecutor, callback)
            awaitClose { telephonyManager.unregisterTelephonyCallback(callback) }
        } else {
            val callback = object : PhoneStateListener() {
                override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                    runCatching { super.onSignalStrengthsChanged(signalStrength) }
                    launch { trySend(getNetworkStatusData()) }
                }

                override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) {
                    // テストコードで super 呼べないため
                    // super、空実装なので呼ぶ必要ない気がするけど一応、、、
                    runCatching { super.onCellInfoChanged(cellInfo) }
                    launch { trySend(getNetworkStatusData()) }
                }

                override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
                    runCatching { super.onDisplayInfoChanged(telephonyDisplayInfo) }
                    tempTelephonyDisplayInfo = telephonyDisplayInfo
                    launch { trySend(getNetworkStatusData()) }
                }
            }
            telephonyManager.listen(callback, PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED or PhoneStateListener.LISTEN_CELL_INFO or PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
            awaitClose { telephonyManager.listen(callback, PhoneStateListener.LISTEN_NONE) }
        }
    }

    /**
     * SIMカードの枚数分だけ subscriptionId を Flow で返す
     * もしSIMカードが1枚も刺さっていない場合はデフォルトの subscriptionId を返します
     *
     * @see [SubscriptionManager.DEFAULT_SUBSCRIPTION_ID]
     */
    @SuppressLint("MissingPermission")
    fun collectMultipleSimSubscriptionIdList(context: Context) = callbackFlow {
        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        if (PermissionCheckTool.isGrantedPermission(context)) {
            // 多分 SubscriptionInfo が更新されたら呼び出される
            val subscriptionInfoCallback = object : SubscriptionManager.OnSubscriptionsChangedListener() {
                override fun onSubscriptionsChanged() {
                    // テストが通らないので runCatching
                    runCatching { super.onSubscriptionsChanged() }
                    val subscriptionIdOrEmptyList = subscriptionManager.activeSubscriptionInfoList?.map { it.subscriptionId } ?: emptyList()
                    val subscriptionIdOrDefaultIdList = subscriptionIdOrEmptyList.ifEmpty { listOf(SubscriptionManager.DEFAULT_SUBSCRIPTION_ID) }
                    trySend(subscriptionIdOrDefaultIdList)
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

    /** [TelephonyManager.requestCellInfoUpdate]を叩く。更新しないと古いのが残ってしまうらしい。非同期なので終わるまで一時停止します */
    @SuppressLint("MissingPermission") // TODO [LogcatPhysicalChannelConfig]を本当にリリースすることになったら別のファイルに
    suspend fun awaitRequestCellInfoUpdate(context: Context, telephonyManager: TelephonyManager) = suspendCoroutine<List<CellInfo>> { continuation ->
        telephonyManager.requestCellInfoUpdate(context.mainExecutor, object : TelephonyManager.CellInfoCallback() {
            override fun onCellInfo(cellInfo: MutableList<CellInfo>) {
                continuation.resume(cellInfo)
            }

            override fun onError(errorCode: Int, detail: Throwable?) {
                continuation.resume(emptyList())
            }
        })
    }

    /**
     * [CellInfo]を簡略化した[BandData]に変換する
     *
     * @param mcc 通信キャリアの MCC。[CellIdentityNr.getMccString]が null の場合はこちらが使われる。
     * @param mnc 通信キャリアの MNC。[CellIdentityNr.getMncString]が null の場合はこちらが使われる。
     * @param cellInfo [TelephonyCallback.CellInfoListener]で取れるやつ
     * @param carrierName キャリア名。[TelephonyManager.getNetworkOperatorName]
     * @param pci PCI
     * @return [BandData]。LTE/NR 以外はnullになります
     * */
    fun convertBandData( // TODO [LogcatPhysicalChannelConfig]を本当にリリースすることになったら別のファイルに
        mcc: String,
        mnc: String,
        cellInfo: CellInfo,
        carrierName: String
    ) = when (val cellIdentity = cellInfo.cellIdentity) {
        // LTE
        is CellIdentityLte -> {
            val earfcn = cellIdentity.earfcn
            BandData(
                isNR = false,
                band = BandDictionaryTool.toLteBand(earfcn),
                earfcn = earfcn,
                frequencyMHz = BandDictionaryTool.toLteFrequencyMhz(earfcn),
                carrierName = carrierName.ifEmpty { cellIdentity.operatorAlphaShort.toString() },
                pci = cellIdentity.pci
            )
        }
        // 5G (NR)
        is CellIdentityNr -> {
            val nrarfcn = cellIdentity.nrarfcn
            // LTE の場合は EARFCNとバンドの表 はすべて単一の結果になるが、
            // NR の場合は同じような表を用意しても、複数の結果になる場合がある（n78はn77を内包のような）
            // なので、まずはシステムから帰ってきた値を優先的に利用し、取得できなかった場合のみ表から探すようにする
            // が、多分モデムのベンダーはこれを実装していないため、おそらく表から探すハメになる
            // ---
            // が、MediaTek の場合はモデムから返ってきた値を極力無視する。
            // NR-ARFCN に関係なく bands が常に [1] の場合があり、全く当てにならない。
            // なので、NR-ARFCN に近しいバンドの場合はそれを採用し、当てにならない場合は近そうなものを採用する
            val modemOrFallbackNrBand = cellIdentity.bands.firstOrNull()
                ?.let { "n$it" }
                ?.let { bandNumber ->
                    if (bandNumber in BandDictionaryTool.toNrBandList(nrarfcn)) {
                        bandNumber
                    } else {
                        // ?: に流れるように
                        null
                    }
                }
                ?: BandDictionaryTool.toNrBand(nrarfcn)

            // 日本だけですが、通信キャリアが使っていない 5G バンドが返ってきたら修正を試みます。
            // MCC MNC は null の可能性があるので引数からも取る
            val fixNrBand = BandDictionaryTool.tryFixNrBand(
                mcc = cellIdentity.mccString ?: mcc,
                mnc = cellIdentity.mncString ?: mnc,
                nrarfcn = nrarfcn,
                bandNumber = modemOrFallbackNrBand
            )

            BandData(
                isNR = true,
                band = fixNrBand,
                earfcn = nrarfcn,
                frequencyMHz = BandDictionaryTool.toNrFrequencyMhz(nrarfcn),
                // キャリア名が空の場合は CellIdentity から取る
                carrierName = carrierName.ifEmpty { cellIdentity.operatorAlphaShort.toString() },
                pci = cellIdentity.pci
            )
        }

        else -> null
    }

    /**
     * [TelephonyDisplayInfo]から簡略化した[NetworkType]を返す
     *
     * @param telephonyDisplayInfo [TelephonyCallback.DisplayInfoListener]で取れるやつ
     * @return [NetworkType]。LTE/NR 以外は [NetworkType.NONE]
     * */
    private fun convertNetworkType(telephonyDisplayInfo: TelephonyDisplayInfo) = when (telephonyDisplayInfo.overrideNetworkType) {
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
    private fun convertStandAloneType(telephonyDisplayInfo: TelephonyDisplayInfo, dataNetworkType: Int) = when {
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


    /** ネットワークの種類 */
    private enum class NetworkType {
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

}