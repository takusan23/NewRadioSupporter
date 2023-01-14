package io.github.takusan23.newradiosupporter.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.takusan23.newradiosupporter.R

/**
 * モバイルデータの設定画面を開くメニュー
 *
 * @param modifier [Modifier]
 * @param onClick 押したら呼ばれる
 */
@ExperimentalMaterial3Api
@Composable
fun OpenMobileNetworkSettingMenu(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        onClick = onClick
    ) {
        CommonItem(
            icon = painterResource(id = R.drawable.ic_outline_open_in_new_24),
            title = stringResource(id = R.string.open_mobile_data_setting_screen_title),
            description = stringResource(id = R.string.open_mobile_data_setting_screen_description)
        )
    }
}