package io.github.takusan23.newradiosupporter.tool

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import io.github.takusan23.newradiosupporter.tool.data.BandData
import io.github.takusan23.newradiosupporter.tool.data.LogcatPhysicalChannelConfigResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.yield
import java.io.InputStreamReader

/**
 * 4G/5G のキャリアアグリゲーションは PhysicalChannelConfig を使うことで取得できるが、
 * プリインストールアプリ権限で保護されていて、通常だと取得できない。
 *
 * だた、PhysicalChannelConfig のログが Logcat に流れていることを発見した。
 * そして Logcat 取得権限は ADB コマンドで付与可能。
 *
 * 権限付与コマンド
 * adb shell pm grant io.github.takusan23.newradiosupporter android.permission.READ_LOGS
 */
object LogcatPhysicalChannelConfig {

    /**
     * TODO Android 15 / 16 のみ動作確認済み
     * PhysicalChannelConfig が入っているログの正規表現
     * https://cs.android.com/android/platform/superproject/main/+/main:frameworks/opt/telephony/src/java/com/android/internal/telephony/NetworkTypeController.java;drc=4b1786fab7e144d982b86c4f1ce00a9982a036a1;l=1340
     */
    private val PHYSICAL_CHANNEL_CONFIG_UPDATE_LOG_REGEX = "\\[(.*?)\\] Physical channel configs updated: anchorNrCell=(.*?), nrBandwidths=(.*?), nrBands=(.*?), configs=(.*)".toRegex()

    /**
     * TODO Android 15 / 16 のみ動作確認済み
     * PhysicalChannelConfig が入っているログからそれぞれの値を取り出す正規表現
     * https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/telephony/java/android/telephony/PhysicalChannelConfig.java;drc=876a342027a418e9d95f12a6a5d9baf2c6d93c4f;l=464
     */
    private val PHYSICAL_CHANNEL_CONFIG_UPDATE_CONFIGS_LOG_REGEX = "\\{mConnectionStatus=(.*?),mCellBandwidthDownlinkKhz=(.*?),mCellBandwidthUplinkKhz=(.*?),mNetworkType=(.*?),mFrequencyRange=(.*?),mDownlinkChannelNumber=(.*?),mUplinkChannelNumber=(.*?),mContextIds=(.*?),mPhysicalCellId=(.*?),mBand=(.*?),mDownlinkFrequency=(.*?),mUplinkFrequency=(.*?)\\}".toRegex()

    /** Logcat から[LogcatPhysicalChannelConfigResult]を取得する */
    @SuppressLint("MissingPermission")
    fun listenLogcatPhysicalChannelConfig(context: Context) = channelFlow {
        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

        // Logcat から PhysicalChannelConfig を得る
        // SIM カードの枚数分購読するので、StateFlow にして使い回す
        val logcatPhysicalChannelConfigFlow = listenLogcatAndConvertPhysicalChannelConfig().stateIn(
            scope = this,
            started = SharingStarted.Lazily,
            initialValue = null
        )

        // SIM カードの枚数分返すので
        // SubscriptionId と LogcatPhysicalChannelConfigResult の Map
        var resultSubscriptionIdAndConfigMap = emptyMap<Int, LogcatPhysicalChannelConfigResult>()
        NetworkStatusFlow.collectMultipleSimSubscriptionIdList(context).collectLatest { subscriptionIdList ->
            coroutineScope {
                subscriptionIdList.forEach { subscriptionId ->
                    // phoneId は SIM スロット番号っぽいので、SubscriptionManager から問い合わせる
                    val simSlotIndex = subscriptionManager.getActiveSubscriptionInfo(subscriptionId).simSlotIndex
                    // SIM カードスロットに対応した TelephonyManager を作る
                    val telephonyManager = (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                        .createForSubscriptionId(subscriptionId)

                    // SIMスロットに対応した Logcat を取り出す
                    logcatPhysicalChannelConfigFlow
                        .filterNotNull()
                        .filter { (updateLog, _) -> updateLog.phoneId == simSlotIndex }
                        .collectLatest { (_, configs) ->

                            // バンドを取得できない端末がある
                            // が、代わりに PCI が取れているので、 getCellInfo() の中に PCI があれば、それを使う
                            val bandList = NetworkStatusFlow.awaitRequestCellInfoUpdate(context, telephonyManager).mapNotNull {
                                // MCC/MNC
                                val (mcc, mnc) = telephonyManager.networkOperator
                                    .let { plmn -> plmn.take(3) to plmn.takeLast(2) }
                                NetworkStatusFlow.convertBandData(
                                    mcc = mcc,
                                    mnc = mnc,
                                    cellInfo = it,
                                    carrierName = telephonyManager.networkOperatorName,
                                )
                            }

                            fun PhysicalChannelConfigLog.convertBandData(): BandData? {
                                // TelephonyManager#getCellInfo の結果に同じ PCI があればそちらを優先
                                return bandList.firstOrNull {
                                    it.pci == mPhysicalCellId?.toIntOrNull()
                                } ?: BandData(
                                    isNR = mNetworkType == "NR",
                                    band = when {
                                        mBand == null -> return null
                                        mNetworkType == "NR" -> "n$mBand"
                                        else -> mBand
                                    },
                                    earfcn = mDownlinkChannelNumber?.toIntOrNull() ?: return null,
                                    carrierName = "",
                                    frequencyMHz = mDownlinkFrequency?.toFloat() ?: return null,
                                    pci = mPhysicalCellId?.toIntOrNull() ?: 0
                                )
                            }

                            val primaryCell = configs
                                .filter { it.mConnectionStatus == "PrimaryServing" }
                                .firstNotNullOfOrNull { it.convertBandData() }
                            val secondaryCellList = configs
                                .filter { it.mConnectionStatus == "SecondaryServing" }
                                .mapNotNull { it.convertBandData() }

                            // 返す
                            val physicalChannelConfigResult = when {

                                // プライマリーセルが取れてないなら、そもそもダメなので null
                                primaryCell == null -> null

                                // セカンダリーセルが複数あればキャリアアグリゲーション
                                2 <= secondaryCellList.size -> LogcatPhysicalChannelConfigResult.CarrierAggregation(
                                    primaryCell = primaryCell,
                                    secondaryCellList = secondaryCellList
                                )

                                // プライマリーセルが 4G で、セカンダリーセルで 5G があれば Endc
                                // セカンダリーセルの情報は、getCellInfo() を優先する。null かもしれないので
                                !primaryCell.isNR && secondaryCellList.any { it.isNR } -> LogcatPhysicalChannelConfigResult.Endc(
                                    primaryCell = primaryCell,
                                    secondaryCell = bandList.firstOrNull { it.isNR } ?: secondaryCellList.first()
                                )

                                // ない
                                else -> null
                            }

                            // 更新して、Flow で送る
                            // 参照が変化しないと Compose が recomposition をトリガーしないので
                            if (physicalChannelConfigResult != null) {
                                resultSubscriptionIdAndConfigMap = resultSubscriptionIdAndConfigMap + (subscriptionId to physicalChannelConfigResult)
                            }
                            trySend(resultSubscriptionIdAndConfigMap)
                        }
                }
            }
        }
    }.filter { it.isNotEmpty() }

    /** Logcat を購読して PhysicalChannelConfig の値を取り出す */
    private fun listenLogcatAndConvertPhysicalChannelConfig() = listenLogcat()
        .filter { it.message.contains("Physical channel configs updated", ignoreCase = true) }
        .map { logCatData ->

            // ログから正規表現で取り出す
            val updateLogRegexResult = PHYSICAL_CHANNEL_CONFIG_UPDATE_LOG_REGEX.find(logCatData.message)?.groupValues
            val updateLog = PhysicalChannelConfigUpdateLog(
                phoneId = updateLogRegexResult?.getOrNull(1)?.toIntOrNull(),
                mLastAnchorNrCellId = updateLogRegexResult?.getOrNull(2),
                mRatchetedNrBandwidths = updateLogRegexResult?.getOrNull(3),
                mRatchetedNrBands = updateLogRegexResult?.getOrNull(4),
                mPhysicalChannelConfigs = updateLogRegexResult?.getOrNull(5)
            )

            // configs が取れていれば
            // 複数の PhysicalChannelConfig（キャリアアグリゲーション） が入ってるので findAll()
            val configsLogRegexResultList = updateLog.mPhysicalChannelConfigs?.let { nonnull -> PHYSICAL_CHANNEL_CONFIG_UPDATE_CONFIGS_LOG_REGEX.findAll(nonnull) }?.toList()
            val configs = configsLogRegexResultList?.map { result ->
                val groupValues = result.groupValues
                PhysicalChannelConfigLog(
                    mConnectionStatus = groupValues.getOrNull(1),
                    mCellBandwidthDownlinkKhz = groupValues.getOrNull(2),
                    mCellBandwidthUplinkKhz = groupValues.getOrNull(3),
                    mNetworkType = groupValues.getOrNull(4),
                    mFrequencyRange = groupValues.getOrNull(5),
                    mDownlinkChannelNumber = groupValues.getOrNull(6),
                    mUplinkChannelNumber = groupValues.getOrNull(7),
                    mContextIds = groupValues.getOrNull(8),
                    mPhysicalCellId = groupValues.getOrNull(9),
                    mBand = groupValues.getOrNull(10),
                    mDownlinkFrequency = groupValues.getOrNull(11),
                    mUplinkFrequency = groupValues.getOrNull(12)
                )
            } ?: emptyList()

            updateLog to configs
        }


    /** Radio の Logcat を取得して Flow で送信する */
    private fun listenLogcat() = flow<LogCatData> {
        val process = Runtime.getRuntime().exec(arrayOf("logcat", "-b", "radio"))
        try {
            InputStreamReader(process.inputStream).buffered().use { bufferedReader ->
                var output: String? = null
                while (bufferedReader.readLine()?.also { output = it } != null) {
                    yield()
                    try {
                        val data = output?.split(" ")?.let {
                            LogCatData(it[0], it[1], it.drop(6).joinToString(separator = " "))
                        } ?: continue
                        emit(data)
                    } catch (e: Exception) {
                        // MediaTek 製デバイスの logcat で ImsPhoneStateListener: [0] のログが文字化けを起こす？
                        // split() が動かなくなるため例外が投げられる
                        // do nothing
                    }
                }
            }
        } finally {
            process.destroy()
        }
    }.flowOn(Dispatchers.IO)

    /** Logcat から取り出した PhysicalChannelConfig */
    private data class PhysicalChannelConfigLog(
        val mConnectionStatus: String?,
        val mCellBandwidthDownlinkKhz: String?,
        val mCellBandwidthUplinkKhz: String?,
        val mNetworkType: String?,
        val mFrequencyRange: String?,
        val mDownlinkChannelNumber: String?,
        val mUplinkChannelNumber: String?,
        val mContextIds: String?,
        val mPhysicalCellId: String?,
        val mBand: String?,
        val mDownlinkFrequency: String?,
        val mUplinkFrequency: String?
    )

    /** PhysicalChanelConfig が入っているログ */
    private data class PhysicalChannelConfigUpdateLog(
        val phoneId: Int?,
        val mLastAnchorNrCellId: String?,
        val mRatchetedNrBandwidths: String?,
        val mRatchetedNrBands: String?,
        val mPhysicalChannelConfigs: String?
    )

    /** Logcat のログ */
    private data class LogCatData(
        val date: String,
        val time: String,
        val message: String
    )
}