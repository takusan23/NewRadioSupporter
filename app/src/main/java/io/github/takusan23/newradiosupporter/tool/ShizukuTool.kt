package io.github.takusan23.newradiosupporter.tool

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ServiceManager
import android.telephony.*
import androidx.annotation.RequiresApi
import com.android.internal.telephony.ISub
import com.android.internal.telephony.ITelephony
import com.android.internal.telephony.ITelephonyRegistry
import io.github.takusan23.newradiosupporter.tool.data.BandData
import io.github.takusan23.newradiosupporter.tool.data.PhysicalChannelConfigData
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
    val subscription: ISub
        get() = ISub.Stub.asInterface(
            ShizukuBinderWrapper(ServiceManager.getService("isub"))
        )

    private val defaultSubscriptionId: Int
        get() = SubscriptionManager.getActiveDataSubscriptionId()

    /** 権限があるか */
    val isShizukuPermissionGranted: Boolean
        get() = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED

    @RequiresApi(Build.VERSION_CODES.S)
    fun collectPhysicalChannelConfigDataList(
        context: Context,
        subscriptionId: Int = defaultSubscriptionId
    ) = channelFlow {
        var physicalChannelConfigList: List<PhysicalChannelConfig> = emptyList()
        var cellInfoList: List<CellInfo> = emptyList()
        val subscriptionInfo = subscription.getActiveSubscriptionInfo(subscriptionId, telephony.currentPackageName, context.attributionTag)
        val simSlotIndex = subscriptionInfo.simSlotIndex + 1
        val carrierName = subscriptionInfo.carrierName.toString()

        fun sendResult() {

            // PhysicalChannelConfig の PhysicalCellId は UID がシステムアプリでないと取得できないようになっていた、
            // ほしいのは EARFCN で、重複を消したらおそらく順番通り入っているのでそのままつかう
            val nrBandList = cellInfoList.filterIsInstance<CellInfoNr>().map {
                BandData.convertBandData(it, carrierName)
            }.distinctBy { it?.band }.filterNotNull()
            val lteBandList = cellInfoList.filterIsInstance<CellInfoLte>().map {
                BandData.convertBandData(it, carrierName)
            }.distinctBy { it?.band }.filterNotNull()

            val nrPhysicalList = physicalChannelConfigList.filter { it.networkType == TelephonyManager.NETWORK_TYPE_NR }.mapIndexed { index, physicalChannelConfig ->
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
                PhysicalChannelConfigData(
                    simSlotIndex = simSlotIndex,
                    cellType = cellType,
                    bandWidthMHz = physicalChannelConfig.cellBandwidthDownlinkKhz / 1000f,
                    networkType = networkType,
                    bandData = nrBandList.getOrNull(index)
                )
            }

            val ltePhysicalList = physicalChannelConfigList.filter {
                it.networkType == TelephonyManager.NETWORK_TYPE_LTE_CA || it.networkType == TelephonyManager.NETWORK_TYPE_LTE
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
                PhysicalChannelConfigData(
                    simSlotIndex = simSlotIndex,
                    cellType = cellType,
                    bandWidthMHz = physicalChannelConfig.cellBandwidthDownlinkKhz / 1000f,
                    networkType = networkType,
                    bandData = lteBandList.getOrNull(index)
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
     * [PhysicalChannelConfig]を取得する
     *
     * @param context [Context]
     * @param subscriptionId SIMカードを選択する際に利用。[SubscriptionManager.getActiveSubscriptionIdList]参照
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun collectPhysicalChannelConfigList(
        context: Context,
        subscriptionId: Int = defaultSubscriptionId
    ) = callbackFlow {
        val callback =
            object : TelephonyCallback(), TelephonyCallback.PhysicalChannelConfigListener {
                override fun onPhysicalChannelConfigChanged(configs: MutableList<PhysicalChannelConfig>?) {
                    trySend(configs ?: emptyList())
                }
            }
        callback.init(context.mainExecutor)
        telephonyRegistry.listenWithEventList(
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
            telephonyRegistry.listenWithEventList(
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
    fun collectCellInfoList(
        context: Context,
        subscriptionId: Int = defaultSubscriptionId
    ) = callbackFlow {
        val callback = object : TelephonyCallback(), TelephonyCallback.CellInfoListener {
            override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) {
                trySend(cellInfo ?: emptyList())
            }
        }

        callback.init(context.mainExecutor)
        telephonyRegistry.listenWithEventList(
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
            telephonyRegistry.listenWithEventList(
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

}