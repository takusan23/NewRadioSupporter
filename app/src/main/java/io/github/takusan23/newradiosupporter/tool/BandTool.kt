package io.github.takusan23.newradiosupporter.tool

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.*
import androidx.core.app.ActivityCompat
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * 昔作ったアプリから拝借
 *
 * [https://github.com/takusan23/MobileStatusWidget/blob/master/app/src/main/java/io/github/takusan23/mobilestatuswidget/tool/MobileDataUsageTool.kt]
 * */
object BandTool {

    /**
     * 現在接続している基地局のバンド番号、周波数チャンネル番号、キャリア名？を返す
     *
     * 失敗したらnullを返す
     *
     * @return 一個目はバンド、二個目は周波数チャンネル番号、三番目はキャリア名（Android 9以降のみ対応）
     * */
    suspend fun getBandDataFromEarfcnOrNrafcn(context: Context): BandData? {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return null
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        // Android Q 以降は最新の値返ってこないらしい（キャッシュを返すため、リクエストが必要）のでコルーチンで解決
        val callIdentity: CellInfo = suspendCoroutine {
            telephonyManager.requestCellInfoUpdate(context.mainExecutor, object : TelephonyManager.CellInfoCallback() {
                override fun onCellInfo(cellInfoList: MutableList<CellInfo>) {
                    if (cellInfoList.isEmpty()) {
                        it.resume(null)
                    } else {
                        val cellInfo = cellInfoList[0]
                        it.resume(cellInfo)
                    }
                }
            })
        } ?: return null

        return when (callIdentity) {
            // LTE
            is CellInfoLte -> {
                val earfcn = callIdentity.cellIdentity.earfcn
                BandData(false, BandDictionary.toLTEBand(earfcn), earfcn, callIdentity.cellIdentity.operatorAlphaShort.toString())
            }
            // 5G (NR)
            is CellInfoNr -> {
                val nrarfcn = (callIdentity.cellIdentity as CellIdentityNr).nrarfcn
                BandData(true, BandDictionary.toNRBand(nrarfcn), nrarfcn, callIdentity.cellIdentity.operatorAlphaShort.toString())
            }
            else -> null
        }
    }

}

data class BandData(
    val isNR: Boolean,
    val band: String,
    val earfcn: Int,
    val carrierName: String,
)