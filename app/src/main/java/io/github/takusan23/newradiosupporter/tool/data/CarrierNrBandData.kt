package io.github.takusan23.newradiosupporter.tool.data

/**
 * 通信キャリアが提供している 5G バンドを定義する
 *
 * @param mcc 通信キャリアの MCC
 * @param mnc 通信キャリアの MNC。複数ある（？）
 * @param provideNrBandList 提供している 5G バンド一覧
 */
data class CarrierNrBandData(
    val mcc: String,
    val mnc: List<String>,
    val provideNrBandList: List<String>
)