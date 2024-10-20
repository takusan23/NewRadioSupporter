package io.github.takusan23.newradiosupporter.ui.screen

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import io.github.takusan23.newradiosupporter.R
import io.github.takusan23.newradiosupporter.ui.WindowInsetsTool
import io.github.takusan23.newradiosupporter.ui.component.BackIcon
import io.github.takusan23.newradiosupporter.ui.component.CommonItem

/**
 * 設定画面
 *
 * @param onNavigate 画面遷移の際に呼ばれる
 * @param onBack 戻るときに呼ばれる
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.setting)) },
                navigationIcon = { BackIcon(onClick = onBack) }
            )
        },
        contentWindowInsets = WindowInsetsTool.ScaffoldWindowInsets
    ) {
        Box(modifier = Modifier.padding(it)) {
            LazyColumn {
                item { OpenPrivacyPolicyNavItem { openPrivacyPolicy(context) } }
                item { OpenSourceCodeSettingNevItem { openGitHub(context) } }
                item { LicenseSettingNavItem { onNavigate(NavigationLinkList.SettingLicenseScreen) } }
            }
        }
    }
}

/** プライバシーポリシーを開く */
private fun openPrivacyPolicy(context: Context) {
    val url = "https://takusan.negitoro.dev/pages/new_radio_supporter_privacy_policy/".toUri()
    context.startActivity(Intent(Intent.ACTION_VIEW, url))
}

/** GitHub を開く */
private fun openGitHub(context: Context) {
    val url = "https://github.com/takusan23/NewRadioSupporter".toUri()
    context.startActivity(Intent(Intent.ACTION_VIEW, url))
}

/**
 * ライセンス画面へ遷移する
 *
 * @param onClick 押したとき
 */
@Composable
private fun LicenseSettingNavItem(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
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
 * プライバシーポリシーを開く
 *
 * @param onClick 押したとき
 */
@Composable
private fun OpenPrivacyPolicyNavItem(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        content = {
            CommonItem(
                icon = painterResource(id = R.drawable.ic_outline_open_in_browser_24),
                title = stringResource(id = R.string.open_privacy_policy),
                description = null
            )
        }
    )
}

/**
 * GitHubを開く
 *
 * @param onClick 押したとき
 */
@Composable
private fun OpenSourceCodeSettingNevItem(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
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