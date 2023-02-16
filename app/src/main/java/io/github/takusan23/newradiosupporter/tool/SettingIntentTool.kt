package io.github.takusan23.newradiosupporter.tool

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.telephony.SubscriptionManager

/** 設定画面を開く */
object SettingIntentTool {

    /**
     * 端末のモバイルデータ設定画面を開く
     * SIMカード選択画面か、モバイルデータ利用量を表示する画面のどちらか
     *
     * @param context [Context]
     */
    fun openMobileDataNetworkSetting(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_MANAGE_ALL_SIM_PROFILES_SETTINGS)
        } else {
            // SIMカード選択
            val defaultSubscriptionId = SubscriptionManager.getDefaultDataSubscriptionId()
            Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS).apply {
                putExtra(Settings.EXTRA_SUB_ID, defaultSubscriptionId)
            }
        }
        context.startActivity(intent)
    }

}