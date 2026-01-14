package io.github.takusan23.newradiosupporter.tool

import io.github.takusan23.newradiosupporter.tool.data.CarrierNrBandData

/**
 * Android API や NR-ARFCN だけでは間違った 5G バンドを返す事があるので、修正を試みる。
 * - Android の API はモデムのベンダーがちゃんと実装していない可能性がある。
 * - また、NR-ARFCN からバンドを求めようにも、複数のバンドに一致する NR-ARFCN がいるため正しいバンドかどうかは分からない。
 *
 * そのため、日本だけですが、通信キャリアが提供している 5G バンドを優先的に探すようにします。
 * そのためには提供しているバンドを用意しておく必要があるので書きました。
 *
 * 出典は
 * バンドは各キャリアのサイト
 * MCC/MNC（PLMN）は総務省より→電気通信番号指定状況
 */
object CarrierNrBandDictionary {

    /** 通信キャリアが提供している 5G バンド */
    private val carrierList = listOf(

        // NTT ドコモ
        CarrierNrBandData(
            mcc = "440",
            mnc = listOf("10"),
            provideNrBandList = listOf(
                "n1",
                "n28",
                "n78",
                "n79",
                "n257"
            )
        ),

        // au
        CarrierNrBandData(
            mcc = "440",
            mnc = listOf("01", "50", "51", "52", "53", "54", "55"),
            provideNrBandList = listOf(
                "n28",
                "n3",
                "n40",
                "n41",
                "n77",
                "n78",
                "n257"
            )
        ),

        // ソフトバンク、ワイモバイル
        CarrierNrBandData(
            mcc = "440",
            mnc = listOf("00", "20", "21"),
            provideNrBandList = listOf(
                "n3",
                "n28",
                "n77",
                "n257"
            )
        ),

        // 楽天モバイル
        CarrierNrBandData(
            mcc = "440",
            mnc = listOf("11"),
            provideNrBandList = listOf(
                "n3", // 転用するの？
                "n28", // 祝 プラチナバンド
                "n77",
                "n257"
            )
        )
    )

    /**
     * 通信キャリアが提供している 5G バンドを取得する
     * あらかじめ用意した配列から探す
     *
     * @param mcc MCC
     * @param mnc MNC
     * @return 通信キャリアが提供している 5G バンド一覧。用意していないキャリアの場合は null
     */
    fun findProvideNrBandNumberList(mcc: String, mnc: String): List<String>? =
        carrierList
            .firstOrNull { carrier -> carrier.mcc == mcc && carrier.mnc.any { it == mnc } }
            ?.provideNrBandList

}
