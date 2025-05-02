package io.github.takusan23.newradiosupporter.tool.data

/** [listenLogcatAndConvertPhysicalChannelConfig]の返り値 */
sealed interface LogcatPhysicalChannelConfigResult {

    /**
     * プライマリーセル、セカンダリーセルの EN-DC 情報
     * アンカーバンドとなっている 4G の情報を取得できます
     */
    data class Endc(
        val primaryCell: BandData,
        val secondaryCell: BandData
    ) : LogcatPhysicalChannelConfigResult

    /**
     * 5G のキャリアアグリゲーション
     * [Endc]と違い、5G が2つ以上あった場合はこちらが返されます
     */
    data class NrCa(
        val primaryCell: BandData,
        val secondaryCellList: List<BandData>
    ) : LogcatPhysicalChannelConfigResult
}