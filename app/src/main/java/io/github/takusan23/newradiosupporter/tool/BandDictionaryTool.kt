package io.github.takusan23.newradiosupporter.tool

/**
 * EARFCN(LTE)と、ARFCN(5G/NR)からバンドを出す
 *
 * LTE band EARFCN table 等で検索検索
 * */
object BandDictionary {

    /**
     * バンドとEARFCN（最小値と最大値）の相対表。LTE（4G）版
     * */
    private val bandLTEList = listOf(
        BandDictionaryData(1, 0, 599),
        BandDictionaryData(2, 600, 1199),
        BandDictionaryData(3, 1200, 1949),
        BandDictionaryData(4, 1950, 2399),
        BandDictionaryData(5, 2400, 2649),
        BandDictionaryData(6, 2650, 2749),
        BandDictionaryData(7, 2750, 3449),
        BandDictionaryData(8, 3450, 3799),
        BandDictionaryData(9, 3800, 4149),
        BandDictionaryData(10, 4150, 4749),
        BandDictionaryData(11, 4750, 4949),
        BandDictionaryData(12, 5010, 5179),
        BandDictionaryData(13, 5180, 5279),
        BandDictionaryData(14, 5280, 5279),
        BandDictionaryData(17, 5730, 5849),
        BandDictionaryData(18, 5850, 5999),
        BandDictionaryData(19, 6000, 6149),
        BandDictionaryData(20, 6150, 6449),
        BandDictionaryData(21, 6450, 6599),
        BandDictionaryData(22, 6600, 7399),
        BandDictionaryData(23, 7500, 7699),
        BandDictionaryData(24, 7700, 8039),
        BandDictionaryData(25, 8040, 8689),
        BandDictionaryData(26, 8690, 9039),
        BandDictionaryData(27, 9040, 9209),
        BandDictionaryData(28, 9210, 9659),
        BandDictionaryData(29, 9660, 9769),
        BandDictionaryData(30, 9770, 9869),
        BandDictionaryData(31, 9870, 9919),
        BandDictionaryData(32, 9920, 10359),
        BandDictionaryData(65, 65536, 66435),
        BandDictionaryData(66, 66436, 67335),
        BandDictionaryData(67, 67336, 67535),
        BandDictionaryData(68, 67536, 67835),
        BandDictionaryData(69, 67836, 68335),
        BandDictionaryData(70, 68336, 68585),
        BandDictionaryData(71, 68586, 68935),
        BandDictionaryData(252, 255144, 256143),
        BandDictionaryData(255, 261519, 262143),
    )

    /**
     * バンドとARFCNの相対表。NR（5G）版
     * */
    private val bandNRList = listOf(
        BandDictionaryData(1, 422000, 434000),
        BandDictionaryData(2, 386000, 398000),
        BandDictionaryData(3, 361000, 376000),
        BandDictionaryData(5, 173800, 178800),
        BandDictionaryData(7, 524000, 538000),
        BandDictionaryData(8, 185000, 192000),
        BandDictionaryData(20, 158200, 164200),
        BandDictionaryData(28, 151600, 160600),
        BandDictionaryData(38, 514000, 524000),
        BandDictionaryData(41, 499200, 537999),
        BandDictionaryData(50, 286400, 303400),
        BandDictionaryData(51, 285400, 286400),
        BandDictionaryData(66, 422000, 440000),
        BandDictionaryData(70, 399000, 404000),
        BandDictionaryData(71, 123400, 130400),
        BandDictionaryData(74, 295000, 303600),
        BandDictionaryData(75, 286400, 303400),
        BandDictionaryData(76, 285400, 286400),
        BandDictionaryData(77, 620000, 680000),
        BandDictionaryData(78, 620000, 653333),
        BandDictionaryData(79, 693334, 733333),
        // 5G ミリ波
        BandDictionaryData(257, 2054167, 2104166),
        BandDictionaryData(258, 2016667, 2070833),
        BandDictionaryData(260, 2229167, 2279166),
    )

    /**
     * 周波数からバンドを出す。LTE版
     *
     * 1850 なら band 3
     * */
    fun toLTEBand(earfcn: Int): String {
        return bandLTEList.find { bandDictionaryData ->
            // 範囲内にあれば
            earfcn in (bandDictionaryData.dlMin..bandDictionaryData.dlMax)
        }?.bandNum.toString()
    }

    /**
     * 周波数からバンドを出す。5G版。NRは「New Radio」らしい
     *
     * n3 とか返ってくると思う
     *
     * 実機を持っていないので知らない
     * */
    fun toNRBand(nrarfcn: Int): String {
        return "n" + bandNRList.find { bandDictionaryData ->
            // 範囲内にあれば
            nrarfcn in (bandDictionaryData.dlMin..bandDictionaryData.dlMax)
        }?.bandNum.toString()
    }

    /** バンド番号からdlMinとdlMaxが取れるように */
    fun getNRBand(bandNum: Int): BandDictionaryData {
        return bandNRList.find { it.bandNum == bandNum }!!
    }

}

/**
 * バンド情報。
 *
 * @param bandNum バンド名
 * @param dlMin earfcnの範囲。最小値
 * @param dlMax earfcnの範囲。最大値
 * */
data class BandDictionaryData(
    val bandNum: Int,
    val dlMin: Int,
    val dlMax: Int,
)