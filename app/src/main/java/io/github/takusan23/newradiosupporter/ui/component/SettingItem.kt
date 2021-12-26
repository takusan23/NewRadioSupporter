package io.github.takusan23.newradiosupporter.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.github.takusan23.newradiosupporter.R

/**
 * ライセンス画面へ遷移する
 *
 * @param onClick 押したとき
 * */
@Composable
fun LicenseSettingNavItem(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        interactionSource = remember { MutableInteractionSource() },
        indication = rememberRipple(),
        onClick = onClick,
        content = {
            CommonItem(
                icon = painterResource(id = R.drawable.ic_outline_info_24),
                title = stringResource(id = R.string.license),
                description = null
            )
        }
    )
}

/**
 * GitHubを開く
 *
 * @param onClick 押したとき
 * */
@Composable
fun OpenSourceCodeSettingNevItem(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        interactionSource = remember { MutableInteractionSource() },
        indication = rememberRipple(),
        onClick = onClick,
        content = {
            CommonItem(
                icon = painterResource(id = R.drawable.ic_outline_open_in_browser_24),
                title = stringResource(id = R.string.open_sourcecode),
                description = stringResource(id = R.string.open_sourcecode_description)
            )
        }
    )
}