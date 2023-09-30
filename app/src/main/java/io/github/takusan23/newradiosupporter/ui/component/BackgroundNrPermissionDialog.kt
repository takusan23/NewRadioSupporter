package io.github.takusan23.newradiosupporter.ui.component

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
import androidx.compose.ui.res.stringResource
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
@Composable
fun BackgroundNrPermissionDialog(
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
                title = stringResource(id = R.string.background_nr_permission_notification_title),
                description = stringResource(id = R.string.background_nr_permission_notification_description),
                permission = android.Manifest.permission.POST_NOTIFICATIONS,
                onResult = { isGranted -> isGrantedNotificationPermission.value = isGranted },
                onDismissRequest = onDismissRequest
            )
            return
        }

        // バックグラウンド位置情報取得権限がない
        if (!isGrantedBackgroundPermission.value) {
            PermissionDialog(
                title = stringResource(id = R.string.background_nr_permission_location_title),
                // %s で backgroundPermissionOptionLabel を埋め込む
                description = stringResource(id = R.string.background_nr_permission_location_description).format(context.packageManager.backgroundPermissionOptionLabel),
                permission = android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                onResult = { isGranted -> isGrantedBackgroundPermission.value = isGranted },
                onDismissRequest = onDismissRequest
            )
            return
        }
    }
}

/**
 * 権限をリクエスト機能を持ったダイアログ
 *
 * @param modifier [Modifier]
 * @param title タイトル
 * @param description 説明
 * @param permission 権限 [android.Manifest.permission]
 * @param onResult 権限をもらえたか
 * @param onDismissRequest ダイアログを消したい時に呼ばれる
 */
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
            ) { Text(stringResource(id = R.string.background_nr_permission_dialog_confirm)) }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) { Text(stringResource(id = R.string.background_nr_permission_dialog_dismiss)) }
        }
    )
}