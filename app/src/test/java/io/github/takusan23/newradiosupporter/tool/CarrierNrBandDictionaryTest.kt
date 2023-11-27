package io.github.takusan23.newradiosupporter.tool

import org.junit.Assert
import org.junit.Test

/** [CarrierNrBandDictionary] のテスト */
class CarrierNrBandDictionaryTest {

    @Test
    fun findProvideNrBandNumberList_既知の通信キャリアは値を返す() {
        val docomo = CarrierNrBandDictionary.findProvideNrBandNumberList("440", "10")
        val au = CarrierNrBandDictionary.findProvideNrBandNumberList("440", "51")
        val softbank = CarrierNrBandDictionary.findProvideNrBandNumberList("440", "20")
        val rakuten = CarrierNrBandDictionary.findProvideNrBandNumberList("440", "11")

        Assert.assertNotNull(docomo)
        Assert.assertNotNull(au)
        Assert.assertNotNull(softbank)
        Assert.assertNotNull(rakuten)
    }

    @Test
    fun findProvideNrBandNumberList_知らない通信キャリアはnullを返す() {
        val unknown = CarrierNrBandDictionary.findProvideNrBandNumberList("999", "99")
        Assert.assertNull(unknown)
    }

}