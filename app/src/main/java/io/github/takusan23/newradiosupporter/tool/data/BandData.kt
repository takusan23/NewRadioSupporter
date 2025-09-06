package io.github.takusan23.newradiosupporter.tool.data

/**
 * バンド情報
 *
 * @param isNR trueなら5G
 * @param band 接続中のバンド番号
 * @param earfcn EARFCN。5GならNRARFCN
 * @param carrierName キャリア名
 * @param frequencyMHz 周波数（MHz）
 * @param pci PCI。これは位置特定につながるため、ユーザーには表示しない
 */
data class BandData(
    val isNR: Boolean,
    val band: String,
    val earfcn: Int,
    val carrierName: String,
    val frequencyMHz: Float,
    val pci: Int
)