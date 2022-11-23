package io.github.takusan23.newradiosupporter.tool.data

import io.github.takusan23.newradiosupporter.tool.FinalNRType
import io.github.takusan23.newradiosupporter.tool.NrStandAloneType

/**
 * [io.github.takusan23.newradiosupporter.tool.NetworkCallbackTool.listenNetworkStatus]の返り値
 *
 * @param simSlotIndex SIMカードスロット番号
 * @param bandData 接続中バンド情報
 * @param finalNRType [FinalNRType]
 * @param nrStandAloneType [NrStandAloneType]
 */
data class NetworkStatusData(
    val simSlotIndex: Int,
    val bandData: BandData,
    val finalNRType: FinalNRType,
    val nrStandAloneType: NrStandAloneType,
)