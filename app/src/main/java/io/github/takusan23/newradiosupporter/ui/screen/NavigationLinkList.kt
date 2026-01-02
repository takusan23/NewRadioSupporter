package io.github.takusan23.newradiosupporter.ui.screen

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 遷移先一覧
 * 型安全も何もそもそも引数がないんだけど、、、
 */
sealed interface NavigationLinkList : NavKey {
    /** 権限下さい画面 */
    @Serializable
    data object PermissionScreen : NavigationLinkList

    /** ホーム画面 */
    @Serializable
    data object HomeScreen : NavigationLinkList

    /** 実質設定画面みたいなやつ */
    @Serializable
    data object SettingScreen : NavigationLinkList

    /** ライセンス画面 */
    @Serializable
    data object SettingLicenseScreen : NavigationLinkList
}