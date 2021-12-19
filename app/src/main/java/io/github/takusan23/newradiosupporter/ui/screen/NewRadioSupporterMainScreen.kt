package io.github.takusan23.newradiosupporter.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.takusan23.newradiosupporter.tool.PermissionCheckTool
import io.github.takusan23.newradiosupporter.ui.theme.NewRadioSupporterTheme
import io.github.takusan23.newradiosupporter.ui.tool.SetNavigationBarColor
import io.github.takusan23.newradiosupporter.ui.tool.SetStatusBarColor

/**
 * 下地になる画面
 * */
@Composable
fun NewRadioSupporterMainScreen() {
    NewRadioSupporterTheme {
        val context = LocalContext.current

        // システムバーの色
        SetStatusBarColor()
        SetNavigationBarColor()

        // ナビゲーション
        val navController = rememberNavController()
        // 権限なければ権限画面へ
        val startDestination = if (PermissionCheckTool.isGranted(context)) NavigationLinkList.HomeScreen else NavigationLinkList.PermissionScreen

        NavHost(navController = navController, startDestination = startDestination) {
            composable(NavigationLinkList.PermissionScreen) {
                PermissionScreen(onGranted = {
                    navController.navigate(NavigationLinkList.HomeScreen)
                })
            }
            composable(NavigationLinkList.HomeScreen) {
                HomeScreen()
            }
        }
    }
}