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
            title = if (isUnlimited) "定額制（無制限のデータ通信）ネットワークに接続中です" else "従量制（データ通信量に制限）ネットワークに接続中です",
            description = "従量制か定額制か"
        )
    }
}