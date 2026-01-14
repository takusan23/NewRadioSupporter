package io.github.takusan23.newradiosupporter.tool

import org.junit.Assert
import org.junit.Test

/** バンド変換テスト */
class BandDictionaryToolTest {

    @Test
    fun toLTEBand_EARFCNをバンドに変換できる() {
        val band3 = BandDictionaryTool.toLteBand(1850)
        Assert.assertEquals(band3, "3")
        val band1 = BandDictionaryTool.toLteBand(276)
        Assert.assertEquals(band1, "1")
        val band19 = BandDictionaryTool.toLteBand(6100)
        Assert.assertEquals(band19, "19")
        val band21 = BandDictionaryTool.toLteBand(6525)
        Assert.assertEquals(band21, "21")
        val band41 = BandDictionaryTool.toLteBand(40340)
        Assert.assertEquals(band41, "41")
    }

    @Test
    fun toLteFrequencyMhz_EARFCNから周波数を計算できる() {
        val frequency_1870 = BandDictionaryTool.toLteFrequencyMhz(1850)
        Assert.assertEquals(frequency_1870, 1870f)
        val frequency_1835 = BandDictionaryTool.toLteFrequencyMhz(1500)
        Assert.assertEquals(frequency_1835, 1835f)
        val frequency_3570_1 = BandDictionaryTool.toLteFrequencyMhz(43291)
        Assert.assertEquals(frequency_3570_1, 3570.1f)
        val frequency_1503_4 = BandDictionaryTool.toLteFrequencyMhz(6525)
        Assert.assertEquals(frequency_1503_4, 1503.4f)
        val frequency_2137_6 = BandDictionaryTool.toLteFrequencyMhz(276)
        Assert.assertEquals(frequency_2137_6, 2137.6f)
    }

    @Test
    fun toNRBand_NRARFCNをバンドに変換できる() {
        val bandN79 = BandDictionaryTool.toNrBandList(703392)
        Assert.assertEquals(bandN79, listOf("n79"))
        // 700MHz 転用
        val bandN28Docomo = BandDictionaryTool.toNrBandList(157690)
        Assert.assertEquals(bandN28Docomo, listOf("n28"))
        val bandN28Au = BandDictionaryTool.toNrBandList(155600)
        Assert.assertEquals(bandN28Au, listOf("n28"))
    }

    @Test
    fun toNrBandList_NRARFCNを複数のバンドに変換できる() {
        // NR-ARFCN が複数のバンドに一致したときのテスト
        val bandN77N78 = BandDictionaryTool.toNrBandList(635424)
        Assert.assertEquals(bandN77N78, listOf("n77", "n78"))
        val bandN257N258 = BandDictionaryTool.toNrBandList(2070015)
        Assert.assertEquals(bandN257N258, listOf("n257", "n258"))
        // NR-ARFCN が 643334 だと n48 n77 n78 に一致する
        val bandN48N77N78 = BandDictionaryTool.toNrBandList(643334)
        Assert.assertEquals(bandN48N77N78, listOf("n48", "n77", "n78"))
        // 700MHz 転用
        val bandN28Softbank = BandDictionaryTool.toNrBandList(159630)
        Assert.assertEquals(bandN28Softbank, listOf("n20", "n28"))
    }

    @Test
    fun isMmWave_ミリ波の判定できる() {
        val bandN257 = BandDictionaryTool.isMmWave(2070015)
        Assert.assertEquals(bandN257, true)
        val bandN78 = BandDictionaryTool.isMmWave(643334)
        Assert.assertEquals(bandN78, false)
    }

    @Test
    fun toFrequencyMHz_NRARFCNから周波数を計算できる() {
        val frequency_798 = 159600
        Assert.assertEquals(BandDictionaryTool.toNrFrequencyMhz(frequency_798), 798f)
        val freqency_3531_36 = 635424
        Assert.assertEquals(BandDictionaryTool.toNrFrequencyMhz(freqency_3531_36), 3531.36f)
        val freqency_3650_01 = 643334
        Assert.assertEquals(BandDictionaryTool.toNrFrequencyMhz(freqency_3650_01), 3650.01f)
        val frequency_27450_96 = 2070015
        Assert.assertEquals(BandDictionaryTool.toNrFrequencyMhz(frequency_27450_96), 27450.96f)
    }

    @Test
    fun isLteFrequency_NRARFCNから転用5Gかどうかの判定ができる() {
        val bandN28 = 157690
        Assert.assertEquals(BandDictionaryTool.isLteFrequency(bandN28), true)
        val bandN78 = 643334
        Assert.assertEquals(BandDictionaryTool.isLteFrequency(bandN78), false)
        val bandN78LteFrequency = 635424
        Assert.assertEquals(BandDictionaryTool.isLteFrequency(bandN78LteFrequency), true)
    }

    @Test
    fun tryFixNrBand_日本の通信キャリアが使っている5Gバンドを優先的に探す() {
        // 対応済みキャリア
        val docomo = "440" to "10"
        val au = "440" to "51"
        val softbank = "440" to "20"
        val rakuten = "440" to "11"
        // バンド n48 n77 n78 に一致する NR-ARFCN
        val nrarfcnN48N77N78 = 643334
        // バンド n20 n28 に一致する NR-ARFCN
        val nrarfcnN20N28 = 159630
        // バンド n38 n41 に一致する NR-ARFCN
        val nrarfcnN38N41 = 522990

        // ドコモ なら n78
        // au softbank 楽天 なら 77 になるべき
        // フォールバックは NR-ARFCN の表から探してきたもの
        nrarfcnN48N77N78.also { nrarfcn ->
            val bandN48N77N78 = BandDictionaryTool.toNrBandList(nrarfcn)
            val maybeBand = bandN48N77N78.first()
            Assert.assertEquals(BandDictionaryTool.tryFixNrBand(docomo.first, docomo.second, nrarfcn, maybeBand), "n78")
            Assert.assertEquals(BandDictionaryTool.tryFixNrBand(au.first, au.second, nrarfcn, maybeBand), "n77")
            Assert.assertEquals(BandDictionaryTool.tryFixNrBand(softbank.first, softbank.second, nrarfcn, maybeBand), "n77")
            Assert.assertEquals(BandDictionaryTool.tryFixNrBand(rakuten.first, rakuten.second, nrarfcn, maybeBand), "n77")
        }

        // ドコモ au softbank 楽天（？） ともに n28 になるべき
        nrarfcnN20N28.also { nrarfcn ->
            val bandN20N28 = BandDictionaryTool.toNrBandList(nrarfcn)
            val maybeBand = bandN20N28.first()
            Assert.assertEquals(BandDictionaryTool.tryFixNrBand(docomo.first, docomo.second, nrarfcn, maybeBand), "n28")
            Assert.assertEquals(BandDictionaryTool.tryFixNrBand(au.first, au.second, nrarfcn, maybeBand), "n28")
            Assert.assertEquals(BandDictionaryTool.tryFixNrBand(softbank.first, softbank.second, nrarfcn, maybeBand), "n28")
            Assert.assertEquals(BandDictionaryTool.tryFixNrBand(rakuten.first, rakuten.second, nrarfcn, maybeBand), "n28")
        }

        // 愚直に計算するとバンド n38 だが、au では提供してないため n41 になるべき
        nrarfcnN38N41.also { nrarfcn ->
            val bandN38N41 = BandDictionaryTool.toNrBandList(nrarfcn)
            val maybeBand = bandN38N41.first()
            Assert.assertEquals(BandDictionaryTool.tryFixNrBand(au.first, au.second, nrarfcn, maybeBand), "n41")
        }
    }

    @Test
    fun tryFixNrBand_知らない通信キャリアの場合はそのまま返す() {
        val unknownCarrier = "999" to "99"
        // バンド n48 n77 n78 に一致する NR-ARFCN
        val nrarfcnN48N77N78 = 643334
        // バンド n20 n28 に一致する NR-ARFCN
        val nrarfcnN20N28 = 159630

        val bandN48 = BandDictionaryTool.toNrBandList(nrarfcnN48N77N78).first()
        val bandN20 = BandDictionaryTool.toNrBandList(nrarfcnN20N28).first()

        Assert.assertEquals(BandDictionaryTool.tryFixNrBand(unknownCarrier.first, unknownCarrier.second, nrarfcnN48N77N78, bandN48), "n48")
        Assert.assertEquals(BandDictionaryTool.tryFixNrBand(unknownCarrier.first, unknownCarrier.second, nrarfcnN20N28, bandN20), "n20")
    }

}