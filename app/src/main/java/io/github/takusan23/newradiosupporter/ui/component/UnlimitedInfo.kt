package io.github.takusan23.newradiosupporter.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import io.github.takusan23.newradiosupporter.R

@Composable
fun UnlimitedInfo(isUnlimited: Boolean) {
    CommonItem(
        icon = painterResource(id = R.drawable.ic_outline_money_24),
        title = if (isUnlimited) "無制限プランを契約中" else "定額制プランを契約中",
        description = "料金プラン"
    )
}