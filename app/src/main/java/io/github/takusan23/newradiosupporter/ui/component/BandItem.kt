package io.github.takusan23.newradiosupporter.ui.component

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.github.takusan23.newradiosupporter.R
import io.github.takusan23.newradiosupporter.tool.data.BandData

@Composable
fun BandItem(
    modifier: Modifier = Modifier,
    bandData: BandData,
) {
    Surface(
        modifier = modifier,
        color = Color.Transparent
    ) {
        if (bandData.isNR) {
            CommonItem(
                icon = painterResource(id = R.drawable.ic_outline_5g_24),
                title = """
                ${stringResource(id = R.string.carrier_name)}：${bandData.carrierName}
                ${stringResource(id = R.string.connecting_band)}：${bandData.band}
                ${stringResource(id = R.string.earfcn)}：${bandData.earfcn}
                ${stringResource(id = R.string.frequency)} : ${bandData.frequencyMHz} MHz
            """.trimIndent(),
                description = stringResource(id = R.string.band_info)
            )
        } else {
            CommonItem(
                icon = painterResource(id = R.drawable.ic_outline_4g_mobiledata_24),
                title = """
                ${stringResource(id = R.string.carrier_name)}：${bandData.carrierName}
                ${stringResource(id = R.string.connecting_band)}：${bandData.band}
                ${stringResource(id = R.string.earfcn)}：${bandData.earfcn}
            """.trimIndent(),
                description = stringResource(id = R.string.band_info)
            )
        }
    }

}