package io.github.takusan23.newradiosupporter.tool

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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
    private val PHYSICAL_CHANNEL_CONFIG_UPDATE_LOG_REGEX = "Physical channel configs updated: anchorNrCell=(.*?), nrBandwidths=(.*?), nrBands=(.*?), configs=(.*)".toRegex()

    /**
     * TODO Android 15 / 16 のみ動作確認済み
     * PhysicalChannelConfig が入っているログからそれぞれの値を取り出す正規表現
     * https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/telephony/java/android/telephony/PhysicalChannelConfig.java;drc=876a342027a418e9d95f12a6a5d9baf2c6d93c4f;l=464
     */
    private val PHYSICAL_CHANNEL_CONFIG_UPDATE_CONFIGS_LOG_REGEX = "\\{mConnectionStatus=(.*?),mCellBandwidthDownlinkKhz=(.*?),mCellBandwidthUplinkKhz=(.*?),mNetworkType=(.*?),mFrequencyRange=(.*?),mDownlinkChannelNumber=(.*?),mUplinkChannelNumber=(.*?),mContextIds=(.*?),mPhysicalCellId=(.*?),mBand=(.*?),mDownlinkFrequency=(.*?),mUplinkFrequency=(.*?)\\}".toRegex()

    /** Logcat を購読して PhysicalChannelConfig の値を取り出す */
    fun listenLogcatPhysicalChannelConfig() = listenLogcat()
        .filter { it.message.contains("Physical channel configs updated", ignoreCase = true) }
        .map { logCatData ->

            // ログから正規表現で取り出す
            val updateLogRegexResult = PHYSICAL_CHANNEL_CONFIG_UPDATE_LOG_REGEX.find(logCatData.message)?.groupValues
            val updateLog = PhysicalChannelConfigUpdateLog(
                mLastAnchorNrCellId = updateLogRegexResult?.getOrNull(1),
                mRatchetedNrBandwidths = updateLogRegexResult?.getOrNull(2),
                mRatchetedNrBands = updateLogRegexResult?.getOrNull(3),
                mPhysicalChannelConfigs = updateLogRegexResult?.getOrNull(4)
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

            LogcatPhysicalChannelConfigData(updateLog, configs)
        }


    /** Radio の Logcat を取得して Flow で送信する */
    private fun listenLogcat() = flow<LogCatData> {
        val process = Runtime.getRuntime().exec(arrayOf("logcat", "-b", "radio"))
        try {
            InputStreamReader(process.inputStream).buffered().use { bufferedReader ->
                var output: String? = null
                while (bufferedReader.readLine()?.also { output = it } != null) {
                    yield()
                    val data = output?.split(" ")?.let {
                        LogCatData(it[0], it[1], it.drop(6).joinToString(separator = " "))
                    } ?: continue
                    emit(data)
                }
            }
        } finally {
            process.destroy()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * [listenLogcatPhysicalChannelConfig]の値
     *
     * @param updateLog Logcat の値
     * @param configs [PhysicalChannelConfigLog]の配列。キャリアアグリゲーションしていれば、ここにバンドとかが格納される。
     */
    data class LogcatPhysicalChannelConfigData(
        val updateLog: PhysicalChannelConfigUpdateLog,
        val configs: List<PhysicalChannelConfigLog>
    )

    /** PhysicalChanelConfig が入っているログ */
    data class PhysicalChannelConfigUpdateLog(
        val mLastAnchorNrCellId: String?,
        val mRatchetedNrBandwidths: String?,
        val mRatchetedNrBands: String?,
        val mPhysicalChannelConfigs: String?
    )

    /** Logcat から取り出した PhysicalChannelConfig */
    data class PhysicalChannelConfigLog(
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

    /** Logcat のログ */
    private data class LogCatData(
        val date: String,
        val time: String,
        val message: String
    )
}