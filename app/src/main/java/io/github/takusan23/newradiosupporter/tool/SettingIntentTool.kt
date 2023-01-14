package io.github.takusan23.newradiosupporter.tool

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.telephony.SubscriptionManager

/** 設定画面を開く */
object SettingIntentTool {

    /**
     * 端末のモバイルデータ詳細画面を開く（通信量とかSIMカード選択画面があるやつ）
     *
     * @param context [Context]
     */
    fun openMobileDataNetworkSetting(context: Context) {
        // SIMカード選択
        val defaultSubscriptionId = SubscriptionManager.getDefaultDataSubscriptionId()
        val intent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS).apply {
            putExtra(Settings.EXTRA_SUB_ID, defaultSubscriptionId)
        }
        context.startActivity(intent)
    }

}