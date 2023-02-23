package io.github.takusan23.newradiosupporter.tool.data

/**
 * 5Gのネットワーク方式、動作未確認
 * Non StandAlone / StandAlone / 5G以外
 */
enum class NrStandAloneType {
    /** 5G スタンドアローン形式 */
    STAND_ALONE,

    /** 5G ノンスタンドアローン形式 */
    NON_STAND_ALONE,

    /** 5G じゃない */
    ERROR
}