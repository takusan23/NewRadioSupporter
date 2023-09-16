package io.github.takusan23.newradiosupporter

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import io.github.takusan23.newradiosupporter.tool.NetworkStatusFlow
import io.github.takusan23.newradiosupporter.tool.data.BandData
import io.github.takusan23.newradiosupporter.tool.data.FinalNrType
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * バックグラウンド5G監視サービス
 * */
class BackgroundNrSupporter : Service() {
    /** SIMカードを監視するスコープ */
    private val scope = MainScope()

    private val notificationManagerCompat by lazy { NotificationManagerCompat.from(this) }
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            stopSelf()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        // TODO Compat がリリースされたら、バージョン分岐消して Compat で書き直す
        fun startForegroundCompat(notification: Notification) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE)
            }
        }

        // とりあえず殺される前に通知出す
        startForegroundCompat(showNotification(null, null))

        // ブロードキャスト登録
        ContextCompat.registerReceiver(this, broadcastReceiver, IntentFilter().apply {
            addAction(STOP_SERVICE_BROADCAST)
        }, ContextCompat.RECEIVER_EXPORTED) // システム（通知押した時）のブロードキャストは exported じゃないとダメ？

        // Flow で監視
        scope.launch {
            NetworkStatusFlow.collectMultipleNetworkStatus(this@BackgroundNrSupporter).collect { statusDataList ->
                statusDataList.forEachIndexed { index, statusData ->
                    val (_, band, type, _) = statusData
                    val notification = showNotification(band, type)
                    if (index == 0) {
                        // 1枚目のSIMはフォアグラウンドサービス通知のために出す
                        startForegroundCompat(notification)
                    } else {
                        // 2 枚目は通知として出す
                        if (ContextCompat.checkSelfPermission(this@BackgroundNrSupporter, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                            notificationManagerCompat.notify(NOTIFICATION_ID + index, notification)
                        }
                    }
                }
            }
        }
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
    private fun showNotification(bandData: BandData?, finalNRType: FinalNrType?): Notification {
        val channelId = "io.github.takusan23.newradiosupporter.NR_SERVICE_NOTIFICATION"
        val channel = NotificationChannelCompat.Builder(channelId, NotificationManagerCompat.IMPORTANCE_LOW).apply {
            setName("バックグラウンド5G通知")
        }.build()
        if (notificationManagerCompat.getNotificationChannel(channelId) == null) {
            notificationManagerCompat.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId).apply {
            val networkType = when (finalNRType) {
                FinalNrType.ANCHOR_BAND -> getString(R.string.type_lte_anchor_band)
                FinalNrType.NR_LTE_FREQUENCY -> getString(R.string.type_lte_freq_nr)
                FinalNrType.NR_SUB6 -> getString(R.string.type_nr_sub6)
                FinalNrType.NR_MMW -> getString(R.string.type_nr_mmwave)
                FinalNrType.LTE -> getString(R.string.type_lte)
                else -> getString(R.string.loading)
            }
            val bandText = if (bandData != null) {
                "${getString(R.string.connecting_band)}：${bandData.band} (${bandData.earfcn})"
            } else getString(R.string.loading)
            setContentTitle(bandData?.carrierName)
            setContentText("$networkType\n$bandText")
            setSmallIcon(
                when (finalNRType) {
                    FinalNrType.ANCHOR_BAND -> R.drawable.ic_android_anchor_lte_band
                    FinalNrType.NR_LTE_FREQUENCY -> R.drawable.android_nr_lte_freq_nr
                    FinalNrType.NR_SUB6 -> R.drawable.ic_android_nr_sub6
                    FinalNrType.NR_MMW -> R.drawable.ic_android_nr_mmw
                    FinalNrType.LTE -> R.drawable.ic_android_lte
                    else -> R.drawable.ic_outline_error_outline_24
                }
            )
            // 通知押したとき
            setContentIntent(PendingIntent.getActivity(this@BackgroundNrSupporter, 1, Intent(this@BackgroundNrSupporter, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
            // 展開時に文字を多く表示させる
            setStyle(NotificationCompat.BigTextStyle().bigText("$networkType\n$bandText"))
            // 終了ボタン
            addAction(R.drawable.ic_outline_close_24, "終了", PendingIntent.getBroadcast(this@BackgroundNrSupporter, 1, Intent(STOP_SERVICE_BROADCAST), PendingIntent.FLAG_IMMUTABLE))
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
            context.startForegroundService(Intent(context, BackgroundNrSupporter::class.java))
        }

        /** サービス終了 */
        fun stopService(context: Context) {
            context.stopService(Intent(context, BackgroundNrSupporter::class.java))
        }

    }

}