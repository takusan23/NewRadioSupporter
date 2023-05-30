package io.github.takusan23.newradiosupporter.tool.data

/**
 * バンドと EARFCN / NRARFCN のデータクラス
 *
 * LTE ( 4G ) の場合は 3GPP TS 36.104 を参照
 * NR ( 5G ) の場合は 3GPP TS 38.101-1 を参照
 */
sealed interface BandDictionaryData {

    /** バンド。n を付けるので String */
    val bandNumber: String

    /** EARFCN / NR-ARFCN の範囲の最小値 */
    val dlMin: Int

    /** EARFCN / NR-ARFCN の範囲の最大値 */
    val dlMax: Int

    /**
     * LTE ( 4G ) のバンド情報
     *
     * @param fDlLow EARFCN から周波数を計算するために必要。MHz（FDL_low）
     */
    data class Lte(
        override val bandNumber: String,
        override val dlMin: Int,
        override val dlMax: Int,
        val fDlLow: Float
    ) : BandDictionaryData

    /**
     * NR ( 5G ) のバンド情報
     *
     * @param isMmWave ミリ波かどうか
     */
    data class Nr(
        override val bandNumber: String,
        override val dlMin: Int,
        override val dlMax: Int,
        val isMmWave: Boolean
    ) : BandDictionaryData
}