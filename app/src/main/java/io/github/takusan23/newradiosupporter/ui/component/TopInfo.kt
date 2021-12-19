package io.github.takusan23.newradiosupporter.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.takusan23.newradiosupporter.R
import io.github.takusan23.newradiosupporter.tool.FinalNRType

/** 一番上に出す情報 */
@Composable
fun TopInfo(
    modifier: Modifier = Modifier,
    finalNRType: FinalNRType,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(100.dp),
            painter = painterResource(id = when (finalNRType) {
                FinalNRType.ANCHOR_LTE_BAND -> R.drawable.ic_android_anchor_lte_band
                FinalNRType.NR_SUB6 -> R.drawable.ic_android_nr_sub6
                FinalNRType.NR_MMW -> R.drawable.ic_android_nr_mmw
                FinalNRType.LTE -> R.drawable.ic_android_lte
            }),
            contentDescription = null
        )
        // メッセージ
        TopMessageInfo(finalNRType = finalNRType)
    }
}

@Composable
private fun TopMessageInfo(finalNRType: FinalNRType) {
    val isNr = finalNRType == FinalNRType.NR_MMW || finalNRType == FinalNRType.NR_SUB6
    Surface(
        modifier = Modifier.padding(10.dp),
        color = animateColorAsState(targetValue = if (isNr) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer).value,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 10.dp),
                painter = painterResource(id = if (isNr) R.drawable.ic_outline_5g_24 else R.drawable.ic_outline_error_outline_24),
                contentDescription = null
            )
            Text(
                text = when (finalNRType) {
                    FinalNRType.ANCHOR_LTE_BAND -> "アンカーLTEバンドの圏内です。表示上では5Gですが、5Gの接続は確立していません。"
                    FinalNRType.NR_SUB6 -> "5GのSub6ネットワークに接続中です。"
                    FinalNRType.NR_MMW -> "5Gのミリ波ネットワークに接続中です。"
                    FinalNRType.LTE -> "4Gに接続中です。"
                },
            )
        }
    }

}