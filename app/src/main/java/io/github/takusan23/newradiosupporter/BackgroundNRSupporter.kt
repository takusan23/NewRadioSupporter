package io.github.takusan23.newradiosupporter

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.takusan23.newradiosupporter.tool.FinalNRType
import io.github.takusan23.newradiosupporter.tool.NetworkCallbackTool
import io.github.takusan23.newradiosupporter.tool.data.BandData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * バックグラウンド5G監視サービス
 * */
class BackgroundNRSupporter : Service() {
    /** SIMカードを監視するスコープ */
    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    /** 回線を監視するFlow。キャンセル用 */
    private val statusCollectFlowJobList = mutableListOf<Job>()

    private val notificationManagerCompat by lazy { NotificationManagerCompat.from(this) }
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            stopSelf()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        // とりあえず殺される前に通知出す
        startForeground(NOTIFICATION_ID, showNotification(null, null))
        // ブロードキャスト登録
        registerReceiver(broadcastReceiver, IntentFilter().apply {
            addAction(STOP_SERVICE_BROADCAST)
        })
        // デュアルSIMに対応させる
        // Flow で監視
        NetworkCallbackTool.listenMultipleSimNetworkStatus(this).onEach { statusFlowList ->
            // 監視してたFlowをキャンセル
            statusCollectFlowJobList.forEach { it.cancel() }
            // SIMの枚数だけ監視する
            statusFlowList.forEachIndexed { index, flow ->
                statusCollectFlowJobList += flow.filterNotNull().onEach { (_, band, type, _) ->
                    val notification = showNotification(band, type)
                    if (index == 0) {
                        // 1枚目のSIMはフォアグラウンドサービス通知のために出す
                        startForeground(NOTIFICATION_ID, notification)
                    } else {
                        // 2枚目は通知として出す
                        notificationManagerCompat.notify(NOTIFICATION_ID + index, notification)
                    }
                }.launchIn(scope)
            }
        }.launchIn(scope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        notificationManagerCompat.cancelAll()
        unregisterReceiver(broadcastReceiver)
    }

    /** 通知を組み立てて、返す */
    private fun showNotification(bandData: BandData?, finalNRType: FinalNRType?): Notification {
        val channelId = "io.github.takusan23.newradiosupporter.NR_SERVICE_NOTIFICATION"
        val channel = NotificationChannelCompat.Builder(channelId, NotificationManagerCompat.IMPORTANCE_LOW).apply {
            setName("バックグラウンド5G通知")
        }.build()
        if (notificationManagerCompat.getNotificationChannel(channelId) == null) {
            notificationManagerCompat.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId).apply {
            val networkType = when (finalNRType) {
                FinalNRType.ANCHOR_BAND -> getString(R.string.type_lte_anchor_band)
                FinalNRType.NR_LTE_FREQUENCY -> getString(R.string.type_lte_freq_nr)
                FinalNRType.NR_SUB6 -> getString(R.string.type_nr_sub6)
                FinalNRType.NR_MMW -> getString(R.string.type_nr_mmwave)
                FinalNRType.LTE -> getString(R.string.type_lte)
                else -> getString(R.string.loading)
            }
            val bandText = if (bandData != null) {
                "${getString(R.string.connecting_band)}：${bandData.band} (${bandData.earfcn})"
            } else getString(R.string.loading)
            setContentTitle(bandData?.carrierName)
            setContentText("$networkType\n$bandText")
            setSmallIcon(when (finalNRType) {
                FinalNRType.ANCHOR_BAND -> R.drawable.ic_android_anchor_lte_band
                FinalNRType.NR_LTE_FREQUENCY -> R.drawable.android_nr_lte_freq_nr
                FinalNRType.NR_SUB6 -> R.drawable.ic_android_nr_sub6
                FinalNRType.NR_MMW -> R.drawable.ic_android_nr_mmw
                FinalNRType.LTE -> R.drawable.ic_android_lte
                else -> R.drawable.ic_outline_error_outline_24
            })
            // 通知押したとき
            setContentIntent(PendingIntent.getActivity(this@BackgroundNRSupporter, 1, Intent(this@BackgroundNRSupporter, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
            // 展開時に文字を多く表示させる
            setStyle(NotificationCompat.BigTextStyle().bigText("$networkType\n$bandText"))
            // 終了ボタン
            addAction(R.drawable.ic_outline_close_24, "終了", PendingIntent.getBroadcast(this@BackgroundNRSupporter, 1, Intent(STOP_SERVICE_BROADCAST), PendingIntent.FLAG_IMMUTABLE))
        }.build().apply {
            // 消せないように。サービス終了時に消える
            flags = NotificationCompat.FLAG_NO_CLEAR
        }
    }

    companion object {

        private const val STOP_SERVICE_BROADCAST = "io.github.takusan23.newradiosupporter.BROADCAST_SERVICE_STOP"

        /** 通知ID。複数SIMの場合はこの値をインクリメントしている。 */
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