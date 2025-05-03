package io.github.takusan23.newradiosupporter

import android.Manifest
import android.app.ActivityManager
import android.app.Notification
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
import android.provider.Settings
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import io.github.takusan23.newradiosupporter.tool.NetworkStatusFlow
import io.github.takusan23.newradiosupporter.tool.data.FinalNrType
import io.github.takusan23.newradiosupporter.tool.data.NetworkStatusData
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
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

        // フォアグラウンドサービス実行中通知を出す
        startForegroundCompat(createRunningNotification())

        // ブロードキャスト登録
        ContextCompat.registerReceiver(this, broadcastReceiver, IntentFilter().apply {
            addAction(STOP_SERVICE_BROADCAST)
        }, ContextCompat.RECEIVER_EXPORTED) // システム（通知押した時）のブロードキャストは exported じゃないとダメ？

        scope.launch {
            // Flow で監視。変化時のみ通知を出す
            NetworkStatusFlow.collectMultipleNetworkStatus(this@BackgroundNrSupporter)
                .distinctUntilChanged()
                .collect { statusDataList ->
                    statusDataList.forEachIndexed { index, statusData ->
                        val notification = createNetworkStatusNotification(statusData)

                        if (ContextCompat.checkSelfPermission(this@BackgroundNrSupporter, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                            notificationManagerCompat.notify(NOTIFICATION_ID + (index + 1), notification)
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

    /** サービス実行中の通知を出す */
    private fun createRunningNotification(): Notification {
        // startForeground 用の通知チャンネルを作成する
        if (notificationManagerCompat.getNotificationChannel(NOTIFICATION_RUNNING_CHANNEL_ID) == null) {
            notificationManagerCompat.createNotificationChannel(
                NotificationChannelCompat.Builder(NOTIFICATION_RUNNING_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW).apply {
                    setName(getString(R.string.background_nr_notification_running_title))
                }.build()
            )
        }
        return NotificationCompat.Builder(this, NOTIFICATION_RUNNING_CHANNEL_ID).apply {
            setContentTitle(getString(R.string.background_nr_notification_running_title))
            setContentText(getString(R.string.background_nr_notification_running_description))
            setSmallIcon(R.drawable.android_nr_supporter)
            // グループ
            setGroup(NOTIFICATION_RUNNING_GROUP_KEY)
            // 通知押したとき
            setContentIntent(PendingIntent.getActivity(this@BackgroundNrSupporter, 1, Intent(this@BackgroundNrSupporter, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
            // 終了ボタン
            addAction(
                R.drawable.ic_outline_close_24,
                getString(R.string.background_nr_notification_exit),
                PendingIntent.getBroadcast(this@BackgroundNrSupporter, 1, Intent(STOP_SERVICE_BROADCAST), PendingIntent.FLAG_IMMUTABLE)
            )
            // 通知設定
            addAction(
                R.drawable.ic_outline_mark_chat_unread_24,
                getString(R.string.background_nr_notification_setting),
                PendingIntent.getActivity(this@BackgroundNrSupporter, 1, Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply { putExtra(Settings.EXTRA_APP_PACKAGE, packageName) }, PendingIntent.FLAG_IMMUTABLE)
            )
        }.build()
    }

    /** 通知を組み立てて、返す */
    private fun createNetworkStatusNotification(statusData: NetworkStatusData): Notification {
        val (simInfo, bandData, finalNRType, _) = statusData

        // 通知チャンネルを作成する
        if (notificationManagerCompat.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
            notificationManagerCompat.createNotificationChannel(
                NotificationChannelCompat.Builder(NOTIFICATION_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW).apply {
                    setName(getString(R.string.background_nr_notification_title))
                }.build()
            )
        }
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).apply {
            // 本文
            val networkType = when (finalNRType) {
                FinalNrType.NR_MMW -> getString(R.string.type_nr_mmwave)
                FinalNrType.NR_SUB6 -> getString(R.string.type_nr_sub6)
                FinalNrType.NR_LTE_FREQUENCY -> getString(R.string.type_lte_freq_nr)
                FinalNrType.MAYBE_NR -> getString(R.string.type_maybe_nr)
                FinalNrType.ANCHOR_BAND -> getString(R.string.type_lte_anchor_band)
                FinalNrType.LTE -> getString(R.string.type_lte)
                FinalNrType.ERROR -> getString(R.string.loading)
            }
            val bandText = "${getString(R.string.connecting_band)}：${bandData.band} (${bandData.earfcn})"
            setContentText("$networkType\n$bandText")

            // タイトル
            val notificationTitle = when (simInfo) {
                is NetworkStatusData.SimInfo.Esim -> getString(R.string.sim_info_esim)
                is NetworkStatusData.SimInfo.PhysicalSim -> "${getString(R.string.sim_info_physical_sim)} ${simInfo.simSlotIndex + 1}" // 0 始まりなので
            }
            setContentTitle("$notificationTitle - ${bandData.carrierName}")

            // そのほか
            setSmallIcon(
                when (finalNRType) {
                    FinalNrType.NR_MMW -> R.drawable.ic_android_nr_mmw
                    FinalNrType.NR_SUB6 -> R.drawable.ic_android_nr_sub6
                    FinalNrType.NR_LTE_FREQUENCY -> R.drawable.android_nr_lte_freq_nr
                    FinalNrType.MAYBE_NR -> R.drawable.ic_outline_error_outline_24
                    FinalNrType.ANCHOR_BAND -> R.drawable.ic_android_anchor_lte_band
                    FinalNrType.LTE -> R.drawable.ic_android_lte
                    FinalNrType.ERROR, null -> R.drawable.ic_outline_error_outline_24
                }
            )
            // グループ
            setGroup(NOTIFICATION_GROUP_KEY)
            // 通知押したとき
            setContentIntent(PendingIntent.getActivity(this@BackgroundNrSupporter, 1, Intent(this@BackgroundNrSupporter, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
            // 展開時に文字を多く表示させる
            setStyle(NotificationCompat.BigTextStyle().bigText("$networkType\n$bandText"))
            // 終了ボタン
            addAction(
                R.drawable.ic_outline_close_24,
                getString(R.string.background_nr_notification_exit),
                PendingIntent.getBroadcast(this@BackgroundNrSupporter, 1, Intent(STOP_SERVICE_BROADCAST), PendingIntent.FLAG_IMMUTABLE)
            )
            // 通知設定
            addAction(
                R.drawable.ic_outline_mark_chat_unread_24,
                getString(R.string.background_nr_notification_setting),
                PendingIntent.getActivity(this@BackgroundNrSupporter, 1, Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply { putExtra(Settings.EXTRA_APP_PACKAGE, packageName) }, PendingIntent.FLAG_IMMUTABLE)
            )
        }.build()
    }

    companion object {

        private const val NOTIFICATION_CHANNEL_ID = "io.github.takusan23.newradiosupporter.NR_SERVICE_NOTIFICATION"
        private const val NOTIFICATION_RUNNING_CHANNEL_ID = "io.github.takusan23.newradiosupporter.NR_SERVICE_RUNNING_NOTIFICATION"

        private const val NOTIFICATION_GROUP_KEY = "GROUP_KEY_NETWORK_STATUS"
        private const val NOTIFICATION_RUNNING_GROUP_KEY = "GROUP_KEY_RUNNING"


        private const val STOP_SERVICE_BROADCAST = "io.github.takusan23.newradiosupporter.BROADCAST_SERVICE_STOP"

        /** 通知ID。複数SIMの場合はこの値をインクリメントしている。 */
        const val NOTIFICATION_ID = 4545

        /** サービス起動・終了を切り替える */
        fun toggleService(context: Context) {
            if (isServiceRunning(context)) {
                stopService(context)
            } else {
                startService(context)
            }
        }

        /**
         * サービスが起動中かどうか
         * 非推奨だが代替案がない...
         *
         * @param context [Context]
         * @return 起動中なら true
         */
        private fun isServiceRunning(context: Context): Boolean {
            return (context.getSystemService(ACTIVITY_SERVICE) as ActivityManager)
                .getRunningServices(Integer.MAX_VALUE)
                .any { it.service.className == BackgroundNrSupporter::class.java.name }
        }

        /** サービス起動 */
        private fun startService(context: Context) {
            context.startForegroundService(Intent(context, BackgroundNrSupporter::class.java))
        }

        /** サービス終了 */
        private fun stopService(context: Context) {
            context.stopService(Intent(context, BackgroundNrSupporter::class.java))
        }

    }

}