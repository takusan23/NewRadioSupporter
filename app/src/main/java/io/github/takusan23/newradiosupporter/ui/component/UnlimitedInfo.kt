package io.github.takusan23.newradiosupporter.ui.component

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import io.github.takusan23.newradiosupporter.R

@Composable
fun UnlimitedInfo(
    modifier: Modifier = Modifier,
    isUnlimited: Boolean,
) {
    Surface(modifier = modifier) {
        CommonItem(
            icon = painterResource(id = R.drawable.ic_outline_money_24),
            title = if (isUnlimited) "無制限プランを契約中" else "従量制プランを契約中",
            description = "料金プラン"
        )
    }
}