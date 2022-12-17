package io.github.takusan23.newradiosupporter.tool

import org.junit.Assert
import org.junit.Test

/** バンド変換テスト */
class BandDictionaryTest {

    @Test
    fun toLTEBand_EARFCNをバンドに変換できる() {
        val band3 = BandDictionary.toLTEBand(1850)
        Assert.assertEquals(band3, "3")
        val band1 = BandDictionary.toLTEBand(100)
        Assert.assertEquals(band1, "1")
        val band41 = BandDictionary.toLTEBand(40340)
        Assert.assertEquals(band41, "41")
    }

    @Test
    fun toNRBand_NRARFCNをバンドに変換できる() {
        val bandN78 = BandDictionary.toNRBand(643334)
        Assert.assertEquals(bandN78, "n78")
        val bandN20 = BandDictionary.toNRBand(159630)
        Assert.assertEquals(bandN20, "n20")
    }

    @Test
    fun isMmWave_ミリ波の判定できる() {
        val bandN257 = BandDictionary.isMmWave(2070015)
        Assert.assertEquals(bandN257, true)
        val bandN78 = BandDictionary.isMmWave(643334)
        Assert.assertEquals(bandN78, false)
    }
}