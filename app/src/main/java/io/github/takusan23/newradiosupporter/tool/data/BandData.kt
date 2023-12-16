package io.github.takusan23.newradiosupporter.tool.data

import android.telephony.*
import io.github.takusan23.newradiosupporter.tool.BandDictionaryTool

/**
 * バンド情報
 *
 * @param isNR trueなら5G
 * @param band 接続中のバンド番号
 * @param earfcn EARFCN。5GならNRARFCN
 * @param carrierName キャリア名
 * @param frequencyMHz 周波数（MHz）
 */
data class BandData(
    val isNR: Boolean,
    val band: String,
    val earfcn: Int,
    val carrierName: String,
    val frequencyMHz: Float
)