package io.github.takusan23.newradiosupporter.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.takusan23.newradiosupporter.R
import io.github.takusan23.newradiosupporter.tool.FinalNRType
import io.github.takusan23.newradiosupporter.tool.NrStandAloneType
import io.github.takusan23.newradiosupporter.tool.data.BandData

/**
 * 回線状態を表示するやつ。未展開時
 *
 * @param modifier [Modifier]
 */
@Composable
fun SimNetworkOverview(
    modifier: Modifier = Modifier,
    simIndex: Int,
    bandData: BandData,
    finalNRType: FinalNRType,
    nrStandAloneType: NrStandAloneType,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = "${stringResource(id = R.string.sim_network_overview_title)} $simIndex - ${bandData.carrierName}"
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_expand_more_24),
                contentDescription = null
            )
        }
        Row(
            modifier = Modifier
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                .height(IntrinsicSize.Max)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    modifier = Modifier.size(40.dp),
                    painter = painterResource(id = when (finalNRType) {
                        FinalNRType.ANCHOR_BAND -> R.drawable.ic_android_anchor_lte_band
                        FinalNRType.NR_SUB6 -> R.drawable.ic_android_nr_sub6
                        FinalNRType.NR_MMW -> R.drawable.ic_android_nr_mmw
                        FinalNRType.LTE -> R.drawable.ic_android_lte
                        else -> R.drawable.ic_outline_info_24
                    }),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.padding(10.dp))
                Text(
                    text = bandData.band,
                    fontSize = 24.sp
                )
            }

            VerticalDivider(modifier = Modifier.padding(5.dp))

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val (icon, strId) = when (nrStandAloneType) {
                    NrStandAloneType.STAND_ALONE -> R.drawable.android_5g_stand_alone to R.string.type_stand_alone_5g_short
                    NrStandAloneType.NON_STAND_ALONE -> R.drawable.android_5g_non_stand_alone to R.string.type_non_stand_alone_5g_short
                    NrStandAloneType.ERROR -> R.drawable.ic_outline_4g_mobiledata_24 to R.string.type_4g_short
                }
                Icon(
                    modifier = Modifier.size(40.dp),
                    painter = painterResource(id = icon),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.padding(10.dp))
                Text(
                    text = stringResource(id = strId),
                    fontSize = 24.sp
                )
            }
        }
    }
}

/**
 * 縦方向の区切り線
 *
 * @param modifier [Modifier]
 */
@Composable
private fun VerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(LocalContentColor.current)
    )
}

/*
@Preview
@Composable
fun SimStatusCardPreview() {
    val bandData = remember { BandData(true, "n78", 643334, "DOCOMO") }
    SimStatusCard(
        modifier = Modifier.height(100.dp),
        bandData = bandData,
        finalNRType = FinalNRType.NR_SUB6,
        nrStandAloneType = NrStandAloneType.STAND_ALONE
    )
}
*/
