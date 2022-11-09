package io.github.takusan23.newradiosupporter.ui.component

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.takusan23.newradiosupporter.R
import io.github.takusan23.newradiosupporter.tool.PermissionCheckTool

/**
 * サービス起動項目。
 * 通知権限がない場合はまず求める。
 *
 * @param modifier [Modifier]
 * @param onClick 押したとき。13以降は通知権限がないと呼ばれない
 */
@ExperimentalMaterial3Api
@Composable
fun BackgroundServiceItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val permissionRequester = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(), onResult = { isGrant ->
        if (isGrant) {
            onClick()
        }
    })

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        onClick = {
            // 13 以降の場合は、通知権限が必要なのでない場合はまず求める
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !PermissionCheckTool.isGrantedPostNotification(context)) {
                permissionRequester.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                onClick()
            }
        }
    ) {
        CommonItem(
            icon = painterResource(id = R.drawable.ic_outline_mark_chat_unread_24),
            title = stringResource(id = R.string.service_state_button),
            description = stringResource(id = R.string.background_service_description),
        )
    }
}