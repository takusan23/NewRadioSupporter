package io.github.takusan23.newradiosupporter.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import io.github.takusan23.newradiosupporter.R
import io.github.takusan23.newradiosupporter.tool.NRType

@Composable
fun NRTypeItem(nrType: NRType) {
    CommonItem(
        icon = painterResource(id = R.drawable.ic_outline_5g_24),
        title = if (nrType == NRType.NR_MW) "ミリ波ネットワークに接続中" else "Sub6ネットワークに接続中",
        description = "接続中5Gの種類"
    )
}

@Composable
fun NRTypeEmptyItem() {
    CommonItem(
        icon = painterResource(id = R.drawable.ic_outline_signal_cellular_connected_no_internet_0_bar_24),
        title = "5Gネットワークには接続していません",
        description = "接続中5Gの種類"
    )
}