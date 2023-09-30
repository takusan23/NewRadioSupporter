package io.github.takusan23.newradiosupporter.tool.data

/**
 * ネットワークの状態のデータクラス
 *
 * @param simSlotIndex SIMカードスロット番号
 * @param bandData 接続中バンド情報
 * @param finalNRType [FinalNRType]
 * @param nrStandAloneType [NrStandAloneType]
 */
data class NetworkStatusData(
    val simSlotIndex: Int,
    val bandData: BandData,
    val finalNRType: FinalNrType,
    val nrStandAloneType: NrStandAloneType,
)