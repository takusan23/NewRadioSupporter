package io.github.takusan23.newradiosupporter.ui.screen

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import io.github.takusan23.newradiosupporter.ui.component.BackIcon
import io.github.takusan23.newradiosupporter.ui.component.LicenseSettingNavItem
import io.github.takusan23.newradiosupporter.ui.component.OpenSourceCodeSettingNevItem

/**
 * 設定画面
 *
 * @param onNavigate 画面遷移の際に呼ばれる
 * @param onBack 戻るときに呼ばれる
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            SmallTopAppBar(
                navigationIcon = { BackIcon(onClick = onBack) },
                title = { Text(text = "設定") }
            )
        },
        content = {
            LazyColumn(content = {
                item { OpenSourceCodeSettingNevItem { openGitHub(context) } }
                item { LicenseSettingNavItem { onNavigate(NavigationLinkList.SettingLicenseScreen) } }
            })
        }
    )
}

/** GitHubを開く */
private fun openGitHub(context: Context) {
    val url = "https://github.com/takusan23/NewRadioSupporter".toUri()
    context.startActivity(Intent(Intent.ACTION_VIEW, url))
}