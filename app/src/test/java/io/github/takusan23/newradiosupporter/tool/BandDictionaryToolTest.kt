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
        val bandN78 = BandDictionaryTool.toNrBand(643334)
        Assert.assertEquals(bandN78, "n78")
        val bandN78_2 = BandDictionaryTool.toNrBand(643296)
        Assert.assertEquals(bandN78_2, "n78")
        val bandN79 = BandDictionaryTool.toNrBand(703392)
        Assert.assertEquals(bandN79, "n79")
        val bandList = BandDictionaryTool.toNrBandList(159630)
        Assert.assertEquals(bandList, listOf("n28", "n20"))
        // 700MHz 転用5G
        val bandN28Docomo = BandDictionaryTool.toNrBand(157690)
        Assert.assertEquals(bandN28Docomo, "n28")
        val bandN28Au = BandDictionaryTool.toNrBand(155600)
        Assert.assertEquals(bandN28Au, "n28")
        val bandN28Softbank = BandDictionaryTool.toNrBand(159630)
        Assert.assertEquals(bandN28Softbank, "n28")
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

}