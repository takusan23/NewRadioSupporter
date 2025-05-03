package io.github.takusan23.newradiosupporter.tool.data

/**
 * ネットワークの状態のデータクラス
 *
 * @param simInfo SIM カード情報
 * @param bandData 接続中バンド情報
 * @param finalNRType [FinalNRType]
 * @param nrStandAloneType [NrStandAloneType]
 */
data class NetworkStatusData(
    val simInfo: SimInfo,
    val bandData: BandData,
    val finalNRType: FinalNrType,
    val nrStandAloneType: NrStandAloneType,
) {

    sealed interface SimInfo {

        /** Android の SubscriptionId */
        val subscriptionId: Int

        /**
         * 物理 SIM
         *
         * @param simSlotIndex SIM カードスロット番号
         */
        data class PhysicalSim(
            override val subscriptionId: Int,
            val simSlotIndex: Int
        ) : SimInfo

        /** eSIM */
        data class Esim(
            override val subscriptionId: Int
        ) : SimInfo

    }

}