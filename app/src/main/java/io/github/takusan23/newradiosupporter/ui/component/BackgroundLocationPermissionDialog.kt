package io.github.takusan23.newradiosupporter.ui.component

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import io.github.takusan23.newradiosupporter.R
import io.github.takusan23.newradiosupporter.tool.PermissionCheckTool

/**
 * バックグラウンド 5G 通知機能を利用する場合は追加で権限くださいダイアログ
 *
 * @param modifier [Modifier]
 * @param onDismissRequest ダイアログを消そうとしたら呼ばれる
 * @param onGranted 権限が全て揃ったら呼ばれる
 */
@SuppressLint("InlinedApi")
@Composable
fun BackgroundLocationPermissionDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onGranted: () -> Unit
) {
    val context = LocalContext.current

    // 権限があるか
    val isGrantedNotificationPermission = remember { mutableStateOf(PermissionCheckTool.isGrantedNotificationPermission(context)) }
    val isGrantedBackgroundPermission = remember { mutableStateOf(PermissionCheckTool.isGrantedBackgroundLocationPermission(context)) }

    // すべて揃った場合は呼び出す
    LaunchedEffect(key1 = isGrantedNotificationPermission.value, key2 = isGrantedBackgroundPermission.value) {
        if (isGrantedNotificationPermission.value && isGrantedBackgroundPermission.value) {
            onGranted()
        }
    }

    Box(modifier = modifier) {

        // 通知権限がない
        if (!isGrantedNotificationPermission.value) {
            PermissionDialog(
                title = "バックグラウンドで動かすために「通知を出す権限」が必要です",
                description = """
                ダイアログが出るので、許可を与えてください。
                
                これらは、バックグラウンドで 5G の状態を通知するためにのみ使われます。
                バックグラウンド 5G 通知機能を利用しない場合はこれらの権限は不要です。
            """.trimIndent(),
                permission = android.Manifest.permission.POST_NOTIFICATIONS,
                onResult = { isGrantedNotificationPermission.value = true },
                onDismissRequest = onDismissRequest
            )
            return
        }

        // バックグラウンド位置情報取得権限がない
        if (!isGrantedBackgroundPermission.value) {
            PermissionDialog(
                title = "バックグラウンドで動かすために「バックグラウンドで位置情報を取得できる権限」が必要です",
                description = """
                位置情報の設定画面に遷移するので、「%s」を選んでください。
                
                これらは、バックグラウンドで 5G の状態を通知するためにのみ使われます。
                バックグラウンド 5G 通知機能を利用しない場合はこれらの権限は不要です。
            """.trimIndent().format(context.packageManager.backgroundPermissionOptionLabel),
                permission = android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                onResult = { isGrantedBackgroundPermission.value = true },
                onDismissRequest = onDismissRequest
            )
            return
        }
    }
}

@Composable
private fun PermissionDialog(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    permission: String,
    onResult: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val permissionRequest = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(), onResult)
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        icon = { Icon(painter = painterResource(id = R.drawable.ic_outline_mark_chat_unread_24), contentDescription = null) },
        title = {
            Text(
                text = title,
                textAlign = TextAlign.Center
            )
        },
        text = { Text(text = description) },
        confirmButton = {
            TextButton(
                onClick = { permissionRequest.launch(permission) }
            ) { Text("権限を付与する") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) { Text("戻る") }
        }
    )
}