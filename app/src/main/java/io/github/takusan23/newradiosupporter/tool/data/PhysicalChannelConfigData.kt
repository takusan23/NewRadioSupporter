package io.github.takusan23.newradiosupporter.tool.data

data class PhysicalChannelConfigData(
    val simSlotIndex: Int,
    val cellType: CellType,
    val bandWidthMHz: Float, // MHz
    val networkType: NetworkType,
    val bandData: BandData
) {

    enum class CellType {
        PRIMARY,
        SECONDARY,
        ERROR,
    }

    enum class NetworkType {
        LTE,
        LTE_CA,
        NR
    }

}