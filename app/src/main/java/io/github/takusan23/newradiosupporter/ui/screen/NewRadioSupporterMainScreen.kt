package io.github.takusan23.newradiosupporter.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
        val navController = rememberNavController()
        // 権限なければ権限画面へ
        val startDestination = if (PermissionCheckTool.isGrantedPermission(context)) NavigationLinkList.HomeScreen else NavigationLinkList.PermissionScreen

        NavHost(navController = navController, startDestination = startDestination) {
            composable(NavigationLinkList.PermissionScreen) {
                PermissionScreen(
                    onGranted = { navController.navigate(NavigationLinkList.HomeScreen) }
                )
            }
            composable(NavigationLinkList.HomeScreen) {
                HomeScreen(
                    onNavigate = { navController.navigate(it) }
                )
            }
            composable(NavigationLinkList.SettingScreen) {
                SettingScreen(
                    onNavigate = { navController.navigate(it) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavigationLinkList.SettingLicenseScreen) {
                LicenseScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}