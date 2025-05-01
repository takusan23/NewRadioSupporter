package io.github.takusan23.newradiosupporter.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.takusan23.newradiosupporter.R
import io.github.takusan23.newradiosupporter.tool.data.FinalNrType
import io.github.takusan23.newradiosupporter.tool.data.NrStandAloneType

/**
 * 回線状態を表示するやつ。展開時用
 *
 * @param nrStandAloneType 5G未接続時はnullでおｋ
 */
@Composable
fun SimNetWorkStatusExpanded(
    modifier: Modifier = Modifier,
    finalNRType: FinalNrType,
    nrStandAloneType: NrStandAloneType?,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        // sub6/mmWave アイコン、NSA/SA アイコン
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {

            Icon(
                modifier = Modifier.size(100.dp),
                painter = painterResource(
                    id = when (finalNRType) {
                        FinalNrType.ANCHOR_BAND -> R.drawable.ic_android_anchor_lte_band
                        FinalNrType.NR_LTE_FREQUENCY -> R.drawable.android_nr_lte_freq_nr
                        FinalNrType.NR_SUB6 -> R.drawable.ic_android_nr_sub6
                        FinalNrType.NR_MMW -> R.drawable.ic_android_nr_mmw
                        FinalNrType.LTE -> R.drawable.ic_android_lte
                        else -> R.drawable.ic_outline_info_24
                    }
                ),
                contentDescription = null
            )

            val iconResId = when (nrStandAloneType) {
                NrStandAloneType.STAND_ALONE -> R.drawable.android_5g_stand_alone
                NrStandAloneType.NON_STAND_ALONE -> R.drawable.android_5g_non_stand_alone
                NrStandAloneType.ERROR, null -> null // 4G なら NSA/SA アイコンを出さない
            }
            if (iconResId != null) {
                Icon(
                    modifier = Modifier.size(100.dp),
                    painter = painterResource(id = iconResId),
                    contentDescription = null
                )
            }
        }

        // メッセージ
        ConnectedStatusMessage(finalNRType = finalNRType)

        // 5Gの場合は NSA/SA どっちかを表示する
        if (nrStandAloneType != null && (finalNRType.isNr)) {
            ConnectedNrStandAloneMessage(nrStandAloneType)
        }
    }
}

/** もし5Gに接続している場合は、スタンドアローン、ノンスタンドアローンのどっちに接続しているかを表示する */
@Composable
private fun ConnectedNrStandAloneMessage(nrStandAloneType: NrStandAloneType) {
    MessageCard(
        cardColor = MaterialTheme.colorScheme.secondaryContainer,
        text = stringResource(
            id = when (nrStandAloneType) {
                NrStandAloneType.STAND_ALONE -> R.string.type_stand_alone_5g
                NrStandAloneType.NON_STAND_ALONE -> R.string.type_non_stand_alone_5g
                NrStandAloneType.ERROR -> R.string.type_lte
            }
        )
    )
}

/** 接続中のネットワークを表示するやつ */
@Composable
private fun ConnectedStatusMessage(finalNRType: FinalNrType) {
    MessageCard(
        cardColor = if (finalNRType.isNr) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.errorContainer
        },
        text = when (finalNRType) {
            FinalNrType.NR_MMW -> stringResource(id = R.string.type_nr_mmwave)
            FinalNrType.NR_SUB6 -> stringResource(id = R.string.type_nr_sub6)
            FinalNrType.NR_LTE_FREQUENCY -> stringResource(id = R.string.type_lte_freq_nr)
            FinalNrType.MAYBE_NR -> stringResource(id = R.string.type_maybe_nr)
            FinalNrType.ANCHOR_BAND -> stringResource(id = R.string.type_lte_anchor_band)
            FinalNrType.LTE -> stringResource(id = R.string.type_lte)
            else -> stringResource(id = R.string.loading)
        }
    )
}

/**
 * メッセージのカード
 *
 * @param cardColor カードの色
 * @param text メッセージ
 */
@Composable
private fun MessageCard(
    modifier: Modifier = Modifier,
    cardColor: Color,
    text: String,
) {
    Surface(
        modifier = modifier,
        color = cardColor,
        shape = RoundedCornerShape(30.dp)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            text = text
        )
    }
}