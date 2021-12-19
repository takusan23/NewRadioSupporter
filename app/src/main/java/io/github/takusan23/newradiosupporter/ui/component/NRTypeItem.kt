package io.github.takusan23.newradiosupporter.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.takusan23.newradiosupporter.R
import io.github.takusan23.newradiosupporter.tool.NetworkType

@Composable
fun NRTypeItem(
    modifier: Modifier = Modifier,
    networkType: NetworkType,
) {
    val isNr = networkType == NetworkType.NR_MMW || networkType == NetworkType.NR_SUB6
    Surface(
        modifier = modifier,
        color = if (isNr) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(10.dp)
    ) {
        CommonItem(
            icon = painterResource(id = when (networkType) {
                NetworkType.NR_SUB6 -> R.drawable.ic_android_nr_sub6
                NetworkType.NR_MMW -> R.drawable.ic_android_nr_mmw
                else -> R.drawable.ic_outline_error_outline_24
            }),
            title = when (networkType) {
                NetworkType.LTE_ADVANCED -> "LTE-Advancedが有効です"
                NetworkType.LTE_CA -> "LTEのキャリアアグリゲーションが有効です"
                NetworkType.NR_MMW -> "ミリ波ネットワークに接続中"
                NetworkType.NR_SUB6 -> "Sub6ネットワークに接続中\nもしくはEN-DC技術によるアンカーLTEバンド圏内です"
                else -> "5Gネットワークに接続していません"
            },
            description = "接続中5Gの種類"
        )
    }
}
