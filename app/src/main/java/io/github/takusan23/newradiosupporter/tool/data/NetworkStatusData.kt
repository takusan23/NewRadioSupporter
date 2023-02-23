package io.github.takusan23.newradiosupporter.tool.data

/**
 * [io.github.takusan23.newradiosupporter.tool.NetworkStatusFlow.collectNetworkStatus]の返り値
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