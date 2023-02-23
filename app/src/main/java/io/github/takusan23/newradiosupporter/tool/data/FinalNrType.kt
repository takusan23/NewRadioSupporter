package io.github.takusan23.newradiosupporter.tool.data

/**
 * 5Gの状態
 * 4G / 5Gかもしれない / 5G Sub6 / 5G mmWave
 */
enum class FinalNrType {

    /** 実際に5Gのミリ波ネットワークに接続している */
    NR_MMW,

    /** 実際に5GのSub6ネットワークに接続している */
    NR_SUB6,

    /** 実際に転用5Gネットワークに接続している */
    NR_LTE_FREQUENCY,

    /** 5Gの可能性がある。バンド情報は取得できないが、5Gの電波強度だけ取得できたパターン */
    MAYBE_NR,

    /** ピクト表示では5Gだが、実はアンカーバンドの圏内であり、5G接続は利用できないことを示す */
    ANCHOR_BAND,

    /** そもそも4Gだしアンカーバンドの圏内にすらいない */
    LTE,

    /** エラー。準備中など */
    ERROR;

    /** 5G ( SUb-6 / ミリ波 / 転用5G ) の場合は true */
    val isNr: Boolean
        get() = this == NR_SUB6 || this == NR_MMW || this == NR_LTE_FREQUENCY
}