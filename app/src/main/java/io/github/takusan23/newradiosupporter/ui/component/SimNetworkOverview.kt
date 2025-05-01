package io.github.takusan23.newradiosupporter.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import io.github.takusan23.newradiosupporter.tool.data.BandData
import io.github.takusan23.newradiosupporter.tool.data.FinalNrType
import io.github.takusan23.newradiosupporter.tool.data.NrStandAloneType

/** 未展開時の情報 */
@Composable
fun SimNetworkOverview(
    modifier: Modifier = Modifier,
    bandData: BandData,
    finalNRType: FinalNrType,
    nrStandAloneType: NrStandAloneType,
) {
    Row(
        modifier = modifier
            .padding(10.dp)
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
