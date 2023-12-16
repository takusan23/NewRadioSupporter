package io.github.takusan23.newradiosupporter.tool

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ServiceManager
import android.sysprop.TelephonyProperties
import android.telephony.*
import androidx.annotation.RequiresApi
import com.android.internal.telephony.IPhoneStateListener
import com.android.internal.telephony.ISub
import com.android.internal.telephony.ITelephony
import com.android.internal.telephony.ITelephonyRegistry
import io.github.takusan23.newradiosupporter.tool.data.BandData
import io.github.takusan23.newradiosupporter.tool.data.PhysicalChannelConfigData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper

/** Shizuku API */
object ShizukuTool {

    private val telephony: ITelephony
        get() = ITelephony.Stub.asInterface(
            ShizukuBinderWrapper(ServiceManager.getService("phone"))
        )

    private val telephonyRegistry: ITelephonyRegistry
        get() = ITelephonyRegistry.Stub.asInterface(
            ShizukuBinderWrapper(ServiceManager.getService("telephony.registry"))
        )

    private val subscription: ISub
        get() = ISub.Stub.asInterface(
            ShizukuBinderWrapper(ServiceManager.getService("isub"))
        )

    private val defaultSubscriptionId: Int
        get() = SubscriptionManager.getActiveDataSubscriptionId()

    /** 権限があるか */
    val isShizukuPermissionGranted: Boolean
        get() = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED

    /**
     * [NetworkStatusFlow.collectMultipleSimSubscriptionIdList]と[collectPhysicalChannelConfigDataList]の合体版
     *
     * @param context [Context]
     * @return SIM カードの枚数分、[PhysicalChannelConfigData]を返す
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun collectMultiplePhysicalChannelConfigDataList(context: Context) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        NetworkStatusFlow.collectMultipleSimSubscriptionIdList(context)
            .transformLatest { subscriptionIdList ->
                // 配列を返したい
                // 配列に配列入れてる、限界、、
                val configDataListArray = Array<List<PhysicalChannelConfigData>>(subscriptionIdList.size) { emptyList() }

                // SIM カードの枚数分 PhysicalChannelConfig を取得する
                subscriptionIdList
                    .mapIndexed { index, subscriptionId ->
                        collectPhysicalChannelConfigDataList(
                            context = context,
                            subscriptionId = subscriptionId
                        ).onEach {
                            configDataListArray[index] = it
                            // 値が来たら transformLatest に渡すことで、この Flow の返り値となる
                            emit(configDataListArray.toList())
                        }
                    }
                    // 複数の Flow を1つにして、購読する
                    .merge()
                    .collect()
            }
    } else emptyFlow()

    /**
     * [PhysicalChannelConfig]と[CellInfo]からセル情報を取得する
     *
     * @return [PhysicalChannelConfigData]
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun collectPhysicalChannelConfigDataList(
        context: Context,
        subscriptionId: Int = defaultSubscriptionId
    ) = channelFlow {
        var physicalChannelConfigList: List<PhysicalChannelConfig> = emptyList()
        var cellInfoList: List<CellInfo> = emptyList()
        val subscriptionInfo: SubscriptionInfo? = subscription.getActiveSubscriptionInfo(subscriptionId, telephony.currentPackageName, context.attributionTag)
        val simSlotIndex = subscriptionInfo?.simSlotIndex?.let { it + 1 } ?: 0
        val carrierName = subscriptionInfo?.carrierName.toString()
        val mcc = subscriptionInfo?.mccString ?: ""
        val mnc = subscriptionInfo?.mncString ?: ""

        fun sendResult() {

            // PhysicalChannelConfig の PhysicalCellId は UID がシステムアプリでないと取得できないようになっていた、
            // ほしいのは EARFCN で、EARFCN の重複を消したら多分 getAllCellInfo と PhysicalChannelConfigの配列 は同じ要素数になるはず？
            val nrBandList = cellInfoList.filterIsInstance<CellInfoNr>().map {
                convertBandData(it, carrierName, mcc, mnc)
            }.distinctBy { it?.earfcn }.filterNotNull()
            val lteBandList = cellInfoList.filterIsInstance<CellInfoLte>().map {
                convertBandData(it, carrierName, mcc, mnc)
            }.distinctBy { it?.earfcn }.filterNotNull()

            val nrPhysicalList = physicalChannelConfigList.filter {
                it.networkType == TelephonyManager.NETWORK_TYPE_NR
            }.mapIndexed { index, physicalChannelConfig ->
                val cellType = when (physicalChannelConfig.connectionStatus) {
                    PhysicalChannelConfig.CONNECTION_PRIMARY_SERVING -> PhysicalChannelConfigData.CellType.PRIMARY
                    PhysicalChannelConfig.CONNECTION_SECONDARY_SERVING -> PhysicalChannelConfigData.CellType.SECONDARY
                    else -> PhysicalChannelConfigData.CellType.ERROR
                }
                val networkType = when (physicalChannelConfig.networkType) {
                    TelephonyManager.NETWORK_TYPE_LTE -> PhysicalChannelConfigData.NetworkType.LTE
                    TelephonyManager.NETWORK_TYPE_LTE_CA -> PhysicalChannelConfigData.NetworkType.LTE_CA
                    TelephonyManager.NETWORK_TYPE_NR -> PhysicalChannelConfigData.NetworkType.NR
                    else -> PhysicalChannelConfigData.NetworkType.LTE
                }
                // downlinkChannelNumber=0 のときは PhysicalChannel ではなく CellInfo から
                val bandData = if (physicalChannelConfig.downlinkChannelNumber == PhysicalChannelConfig.CHANNEL_NUMBER_UNKNOWN ) {
                    nrBandList.getOrNull(index)
                } else {
                    convertBandData(physicalChannelConfig, carrierName, mcc, mnc)
                }
                PhysicalChannelConfigData(
                    simSlotIndex = simSlotIndex,
                    cellType = cellType,
                    bandWidthMHz = physicalChannelConfig.cellBandwidthDownlinkKhz / 1000f,
                    networkType = networkType,
                    // 0 のときはリストから探してくる
                    bandData = bandData
                )
            }

            val ltePhysicalList = physicalChannelConfigList.filter {
                it.networkType == TelephonyManager.NETWORK_TYPE_LTE_CA || it.networkType == TelephonyManager.NETWORK_TYPE_LTE
            }.mapIndexed { index, physicalChannelConfig ->
                val cellType = when (physicalChannelConfig.connectionStatus) {
                    CellInfo.CONNECTION_PRIMARY_SERVING -> PhysicalChannelConfigData.CellType.PRIMARY
                    CellInfo.CONNECTION_SECONDARY_SERVING -> PhysicalChannelConfigData.CellType.SECONDARY
                    else -> PhysicalChannelConfigData.CellType.ERROR
                }
                val networkType = when (physicalChannelConfig.networkType) {
                    TelephonyManager.NETWORK_TYPE_LTE -> PhysicalChannelConfigData.NetworkType.LTE
                    TelephonyManager.NETWORK_TYPE_LTE_CA -> PhysicalChannelConfigData.NetworkType.LTE_CA
                    TelephonyManager.NETWORK_TYPE_NR -> PhysicalChannelConfigData.NetworkType.NR
                    else -> PhysicalChannelConfigData.NetworkType.LTE
                }
                // downlinkChannelNumber=0 のときは PhysicalChannel ではなく CellInfo から
                val bandData = if (physicalChannelConfig.downlinkChannelNumber == PhysicalChannelConfig.CHANNEL_NUMBER_UNKNOWN) {
                    lteBandList.getOrNull(index)
                } else {
                    convertBandData(physicalChannelConfig, carrierName, mcc, mnc)
                }
                PhysicalChannelConfigData(
                    simSlotIndex = simSlotIndex,
                    cellType = cellType,
                    bandWidthMHz = physicalChannelConfig.cellBandwidthDownlinkKhz / 1000f,
                    networkType = networkType,
                    bandData = bandData
                )
            }

            trySend(nrPhysicalList + ltePhysicalList)
        }

        launch {
            collectPhysicalChannelConfigList(context, subscriptionId).collect {
                physicalChannelConfigList = it
                sendResult()
            }
        }
        launch {
            collectCellInfoList(context, subscriptionId).collect {
                cellInfoList = it
                sendResult()
            }
        }
    }

    /**
     * [CellInfo]を簡略化した[BandData]に変換する
     *
     * @param cellInfo [TelephonyCallback.CellInfoListener]で取れるやつ
     * @param carrierName キャリア名。[TelephonyManager.getNetworkOperatorName]
     * @return [BandData]。LTE/NR 以外はnullになります
     */
    private fun convertBandData(
        cellInfo: CellInfo,
        carrierName: String,
        mcc: String,
        mnc: String
    ) = when (val cellIdentity = cellInfo.cellIdentity) {
        // LTE
        is CellIdentityLte -> {
            val earfcn = cellIdentity.earfcn
            BandData(
                isNR = false,
                band = BandDictionaryTool.toLteBand(earfcn),
                earfcn = earfcn,
                carrierName = carrierName,
                frequencyMHz = BandDictionaryTool.toLteFrequencyMhz(earfcn),
            )
        }
        // 5G (NR)
        is CellIdentityNr -> {
            val nrarfcn = cellIdentity.nrarfcn
            // 日本だけですが、通信キャリアが使っていない 5G バンドが返ってきたら修正を試みます。
            // MCC MNC は null の可能性があるので引数からも取る
            val modemOrFallbackNrBand = cellIdentity.bands.firstOrNull()?.let { "n$it" } ?: BandDictionaryTool.toNrBand(nrarfcn)
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
                carrierName = carrierName,
                frequencyMHz = BandDictionaryTool.toNrFrequencyMhz(nrarfcn),
            )
        }

        else -> null
    }

    /**
     * [PhysicalChannelConfig]を簡略化した[BandData]に変換する
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun convertBandData(
        physicalChannelConfig: PhysicalChannelConfig,
        carrierName: String,
        mcc: String,
        mnc: String
    ): BandData? {
        return when (physicalChannelConfig.networkType) {
            TelephonyManager.NETWORK_TYPE_LTE -> {
                val earfcn = physicalChannelConfig.downlinkChannelNumber
                BandData(
                    isNR = false,
                    band = BandDictionaryTool.toLteBand(earfcn),
                    earfcn = earfcn,
                    carrierName = carrierName,
                    frequencyMHz = BandDictionaryTool.toLteFrequencyMhz(earfcn),
                )
            }

            TelephonyManager.NETWORK_TYPE_NR -> {
                val nrarfcn = physicalChannelConfig.downlinkChannelNumber
                // 日本だけですが、通信キャリアが使っていない 5G バンドが返ってきたら修正を試みます。
                // MCC MNC は null の可能性があるので引数からも取る
                val modemOrFallbackNrBand = physicalChannelConfig.band.let {
                    if (it != PhysicalChannelConfig.BAND_UNKNOWN) {
                        "n$it"
                    } else {
                        BandDictionaryTool.toNrBand(nrarfcn)
                    }
                }
                val fixNrBand = BandDictionaryTool.tryFixNrBand(
                    mcc = mcc,
                    mnc = mnc,
                    nrarfcn = nrarfcn,
                    bandNumber = modemOrFallbackNrBand
                )

                BandData(
                    isNR = true,
                    band = fixNrBand,
                    earfcn = nrarfcn,
                    carrierName = carrierName,
                    frequencyMHz = BandDictionaryTool.toNrFrequencyMhz(nrarfcn),
                )
            }

            else -> null
        }
    }

    /**
     * [PhysicalChannelConfig]を取得する
     *
     * @param context [Context]
     * @param subscriptionId SIMカードを選択する際に利用。[SubscriptionManager.getActiveSubscriptionIdList]参照
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun collectPhysicalChannelConfigList(
        context: Context,
        subscriptionId: Int = defaultSubscriptionId
    ) = callbackFlow {
        val callback = object : TelephonyCallback(), TelephonyCallback.PhysicalChannelConfigListener {
            override fun onPhysicalChannelConfigChanged(configs: MutableList<PhysicalChannelConfig>?) {
                trySend(configs ?: emptyList())
            }
        }
        callback.init(context.mainExecutor)
        telephonyRegistry.compatListenWithEventList(
            true,
            true,
            subscriptionId,
            telephony.currentPackageName,
            context.attributionTag,
            callback.callback,
            intArrayOf(TelephonyCallback.EVENT_PHYSICAL_CHANNEL_CONFIG_CHANGED),
            true
        )

        awaitClose {
            telephonyRegistry.compatListenWithEventList(
                false,
                false,
                subscriptionId,
                telephony.currentPackageName,
                context.attributionTag,
                callback.callback,
                intArrayOf(0),
                false
            )
        }
    }

    /**
     * [CellInfo]を取得する
     * TODO 公開APIで周辺セルの取得は可能です。
     *
     * @param context [Context]
     * @param subscriptionId SIMカードを選択する際に利用。[SubscriptionManager.getActiveSubscriptionIdList]参照
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun collectCellInfoList(
        context: Context,
        subscriptionId: Int = defaultSubscriptionId
    ) = callbackFlow {
        val callback = object : TelephonyCallback(), TelephonyCallback.CellInfoListener {
            override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) {
                trySend(cellInfo ?: emptyList())
            }
        }

        callback.init(context.mainExecutor)
        telephonyRegistry.compatListenWithEventList(
            false,
            false,
            subscriptionId,
            telephony.currentPackageName,
            context.attributionTag,
            callback.callback,
            intArrayOf(TelephonyCallback.EVENT_CELL_INFO_CHANGED),
            true
        )
        awaitClose {
            telephonyRegistry.compatListenWithEventList(
                false,
                false,
                subscriptionId,
                telephony.currentPackageName,
                context.attributionTag,
                callback.callback,
                intArrayOf(0),
                false
            )
        }
    }


    /** [ITelephonyRegistry.listenWithEventList]を後方互換性を持たせて呼び出す拡張関数 */
    private fun ITelephonyRegistry.compatListenWithEventList(
        renounceFineLocationAccess: Boolean,
        renounceCoarseLocationAccess: Boolean,
        subId: Int,
        callingPackage: String?,
        callingFeatureId: String?,
        callback: IPhoneStateListener?,
        events: IntArray?,
        notifyNow: Boolean
    ) {
        // バージョンによってメソッドの引数が違う（そもそも公開APIではないので当たり前ではある）
        // リフレクションを使ってバージョンが違っても落ちないように
        // 引数は AOSP のコード参照
        val listenWithEventListMethod = this::class.java
            .methods
            .first { it.name == "listenWithEventList" }
        when {
            // Android 13 以上
            // https://cs.android.com/android/platform/superproject/+/android-13.0.0_r1:frameworks/base/services/core/java/com/android/server/TelephonyRegistry.java;l=1030
            Build.VERSION_CODES.TIRAMISU <= Build.VERSION.SDK_INT -> {
                listenWithEventListMethod.invoke(
                    this,
                    renounceFineLocationAccess,
                    renounceCoarseLocationAccess,
                    subId,
                    callingPackage,
                    callingFeatureId,
                    callback,
                    events,
                    notifyNow
                )
            }
            // Android 12 以下
            // https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/services/core/java/com/android/server/TelephonyRegistry.java;l=993
            Build.VERSION_CODES.S >= Build.VERSION.SDK_INT -> {
                listenWithEventListMethod.invoke(
                    this,
                    subId,
                    callingPackage,
                    callingFeatureId,
                    callback,
                    events,
                    notifyNow
                )
            }
        }
    }

}