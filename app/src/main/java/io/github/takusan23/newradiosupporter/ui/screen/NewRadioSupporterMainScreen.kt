package io.github.takusan23.newradiosupporter.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.github.takusan23.newradiosupporter.tool.PermissionCheckTool
import io.github.takusan23.newradiosupporter.ui.theme.NewRadioSupporterTheme

/**
 * 下地になる画面
 * */
@Composable
fun NewRadioSupporterMainScreen() {
    NewRadioSupporterTheme {
        val context = LocalContext.current
        // ナビゲーション
        // 権限なければ権限画面へ
        val backStack = rememberNavBackStack(if (PermissionCheckTool.isGrantedPermission(context)) NavigationLinkList.HomeScreen else NavigationLinkList.PermissionScreen)

        NavDisplay(
            backStack = backStack,
            entryProvider = entryProvider {
                entry<NavigationLinkList.PermissionScreen> {
                    PermissionScreen(
                        onGranted = {
                            // 履歴配列から消してホームを出す
                            backStack += NavigationLinkList.HomeScreen
                            backStack -= NavigationLinkList.PermissionScreen
                        }
                    )
                }
                entry<NavigationLinkList.HomeScreen> {
                    HomeScreen(
                        onNavigate = { dest -> backStack += dest }
                    )
                }
                entry<NavigationLinkList.SettingScreen> {
                    SettingScreen(
                        onNavigate = { dest -> backStack += dest },
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                entry<NavigationLinkList.SettingLicenseScreen> {
                    LicenseScreen(
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
            }
        )
    }
}