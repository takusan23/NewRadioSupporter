package io.github.takusan23.newradiosupporter.tool.data

import android.telephony.*
import io.github.takusan23.newradiosupporter.tool.BandDictionary

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
) {
    companion object {
        /**
         * [CellInfo]を簡略化した[BandData]に変換する
         *
         * @param cellInfo [TelephonyCallback.CellInfoListener]で取れるやつ
         * @param carrierName キャリア名。[TelephonyManager.getNetworkOperatorName]
         * @return [BandData]。LTE/NR 以外はnullになります
         * */
        fun convertBandData(cellInfo: CellInfo, carrierName: String) = when (val cellIdentity = cellInfo.cellIdentity) {
            // LTE
            is CellIdentityLte -> {
                val earfcn = cellIdentity.earfcn
                BandData(
                    isNR = false,
                    band = BandDictionary.toLteBand(earfcn),
                    earfcn = earfcn,
                    carrierName = carrierName,
                    frequencyMHz = BandDictionary.toLteFrequencyMhz(earfcn),
                )
            }
            // 5G (NR)
            is CellIdentityNr -> {
                val nrarfcn = cellIdentity.nrarfcn
                BandData(
                    isNR = true,
                    band = BandDictionary.toNrBand(nrarfcn),
                    earfcn = nrarfcn,
                    carrierName = carrierName,
                    frequencyMHz = BandDictionary.toNrFrequencyMhz(nrarfcn),
                )
            }
            else -> null
        }
    }
}