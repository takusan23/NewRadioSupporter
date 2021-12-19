package io.github.takusan23.newradiosupporter

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.takusan23.newradiosupporter.tool.FinalNRType
import io.github.takusan23.newradiosupporter.tool.NetworkCallback
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * バックグラウンド5G監視サービス
 * */
class BackgroundNRSupporter : Service() {

    private val notificationManagerCompat by lazy { NotificationManagerCompat.from(this) }
    private val scope = CoroutineScope(Dispatchers.Main)
    private val STOP_SERVICE_BROADCAST = "io.github.takusan23.newradiosupporter.BROADCAST_SERVICE_STOP"
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            stopSelf()
        }
    }

    override fun onBind(intent: Intent) = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        // とりあえず殺される前に通知出す
        showNotification(null)
        // ブロードキャスト登録
        registerReceiver(broadcastReceiver, IntentFilter().apply {
            addAction(STOP_SERVICE_BROADCAST)
        })
        // Flowで収集する
        scope.launch {
            // Flowを結合する
            val bandInfoFlow = NetworkCallback.listenBand(this@BackgroundNRSupporter)
            val networkType = NetworkCallback.listenNetworkStatus(this@BackgroundNRSupporter)
            combine(bandInfoFlow, networkType, ::Pair)
                .map { (bandData, type) ->
                    if (bandData != null) {
                        NetworkCallback.finalResult(bandData, type)
                    } else FinalNRType.LTE
                }
                .collect { finalType ->
                    showNotification(finalType)
                }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        unregisterReceiver(broadcastReceiver)
    }

    private fun showNotification(finalNRType: FinalNRType? = null) {
        val channelId = "io.github.takusan23.newradiosupporter.NR_SERVICE_NOTIFICATION"
        val channel = NotificationChannelCompat.Builder(channelId, NotificationManagerCompat.IMPORTANCE_LOW).apply {
            setName("バックグラウンド5Gサポーター")
        }.build()
        if (notificationManagerCompat.getNotificationChannel(channelId) == null) {
            notificationManagerCompat.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId).apply {
            setContentTitle(when (finalNRType) {
                FinalNRType.NR_SUB6, FinalNRType.NR_MMW -> "5Gに接続中です"
                FinalNRType.ANCHOR_LTE_BAND, FinalNRType.LTE -> "4Gに接続中です"
                null -> "準備中です"
            })
            setContentText(when (finalNRType) {
                FinalNRType.ANCHOR_LTE_BAND -> "アンカーLTEバンドの圏内です。表示上では5Gですが、5Gの接続は確立していません。"
                FinalNRType.NR_SUB6 -> "5GのSub6ネットワークに接続中です。"
                FinalNRType.NR_MMW -> "5Gのミリ波ネットワークに接続中です。"
                FinalNRType.LTE -> "4Gに接続中です。"
                null -> "準備中です"
            })
            setSmallIcon(when (finalNRType) {
                FinalNRType.ANCHOR_LTE_BAND -> R.drawable.ic_android_anchor_lte_band
                FinalNRType.NR_SUB6 -> R.drawable.ic_android_nr_sub6
                FinalNRType.NR_MMW -> R.drawable.ic_android_nr_mmw
                FinalNRType.LTE -> R.drawable.ic_android_lte
                null -> R.drawable.ic_outline_error_outline_24
            })
            // 通知押したとき
            setContentIntent(PendingIntent.getActivity(this@BackgroundNRSupporter, 1, Intent(this@BackgroundNRSupporter, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
            // 終了ボタン
            addAction(R.drawable.ic_outline_close_24, "終了", PendingIntent.getBroadcast(this@BackgroundNRSupporter, 1, Intent(STOP_SERVICE_BROADCAST), PendingIntent.FLAG_IMMUTABLE))
        }.build()
        startForeground(NOTIFICATION_ID, notification)
    }

    companion object {

        const val NOTIFICATION_ID = 4545

        /** サービスが起動中かどうか */
        fun isServiceRunning(context: Context): Boolean {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return notificationManager.activeNotifications.any { it.id == NOTIFICATION_ID }
        }

        /** サービス起動 */
        fun startService(context: Context) {
            context.startForegroundService(Intent(context, BackgroundNRSupporter::class.java))
        }

        /** サービス終了 */
        fun stopService(context: Context) {
            context.stopService(Intent(context, BackgroundNRSupporter::class.java))
        }

    }

}