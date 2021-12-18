package io.github.takusan23.newradiosupporter.tool

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionCheckTool {

    /**
     * 必要な権限があるかどうか
     *
     * @param context [Context]
     * @return 権限があればtrue
     * */
    fun isGranted(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val readPhoneState = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
        return (fineLocation == PackageManager.PERMISSION_GRANTED) && (readPhoneState == PackageManager.PERMISSION_GRANTED)
    }

}