package io.github.takusan23.newradiosupporter.tool

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

object PermissionCheckTool {

    /**
     * 必要な権限があるかどうか
     *
     * @param context [Context]
     * @return 権限があればtrue
     * */
    fun isGranted(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
        val readPhoneState = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE)
        return (fineLocation == PackageManager.PERMISSION_GRANTED) && (readPhoneState == PackageManager.PERMISSION_GRANTED)
    }

    /**
     * 通知権限があるかどうか。
     * Android 13 以降のみ対応
     *
     * @param context [Context]
     * @return 権限があればtrue
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun isGrantedPostNotification(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

}