package io.github.takusan23.newradiosupporter.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.takusan23.newradiosupporter.R
import io.github.takusan23.newradiosupporter.tool.data.BandData

/** バンド / NR-ARFCN / 周波数表示 */
@Composable
fun BandItem(
    modifier: Modifier = Modifier,
    bandData: BandData
) {

    Row(modifier) {

        // バンド
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.connecting_band),
                fontSize = 15.sp
            )
            Text(
                text = bandData.band,
                fontSize = 35.sp
            )
        }

        // NR-ARFCN / 周波数
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.frequency),
                fontSize = 15.sp
            )
            Text(
                text = "${bandData.frequencyMHz} MHz",
                fontSize = 25.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(id = if (bandData.isNR) R.string.nr_earfcn else R.string.earfcn),
                fontSize = 15.sp
            )
            Text(
                text = bandData.earfcn.toString(),
                fontSize = 25.sp
            )
        }
    }
}