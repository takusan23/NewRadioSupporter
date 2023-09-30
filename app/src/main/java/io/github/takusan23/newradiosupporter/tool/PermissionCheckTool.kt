package io.github.takusan23.newradiosupporter.tool

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionCheckTool {

    /**
     * このアプリを動かすのに最低限必要な権限があるか
     *
     * @param context [Context]
     * @return 権限があれば true
     * */
    fun isGrantedPermission(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
        val readPhoneState = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE)
        return (fineLocation == PackageManager.PERMISSION_GRANTED) && (readPhoneState == PackageManager.PERMISSION_GRANTED)
    }

    /**
     * 通知権限があるかどうか。
     * Android 12 以下は常に true を返す
     *
     * @param context [Context]
     * @return 権限があれば true
     */
    fun isGrantedNotificationPermission(context: Context): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    /**
     * バックグラウンドで位置情報を取得する権限があるかどうか。
     *
     * @param context [Context]
     * @return 権限があれば true
     */
    fun isGrantedBackgroundLocationPermission(context: Context): Boolean = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED

}