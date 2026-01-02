package io.github.takusan23.newradiosupporter.ui.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
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
import androidx.lifecycle.compose.dropUnlessResumed
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
    onNavigate: (NavigationLinkList) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.setting)) },
                navigationIcon = { BackIcon(onClick = dropUnlessResumed(block = onBack)) }
            )
        },
        contentWindowInsets = WindowInsetsTool.ScaffoldWindowInsets
    ) {
        Box(modifier = Modifier.padding(it)) {
            LazyColumn {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    item { OpenLanguageSettingNavItem { openLanguageSetting(context) } }
                }
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
 * 言語変更の設定画面を開く
 * OS レベルで言語変更機能がついたのが Android 13 以上。
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun openLanguageSetting(context: Context) {
    val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
    intent.data = Uri.fromParts("package", context.packageName, null)
    context.startActivity(intent)
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

/**
 * 言語設定画面を開く
 *
 * @param onClick 押したとき
 */
@Composable
private fun OpenLanguageSettingNavItem(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        content = {
            CommonItem(
                icon = painterResource(id = R.drawable.ic_translate_24),
                title = stringResource(id = R.string.open_language_setting),
                description = null
            )
        }
    )
}