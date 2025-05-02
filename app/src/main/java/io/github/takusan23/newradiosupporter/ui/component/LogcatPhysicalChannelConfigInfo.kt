package io.github.takusan23.newradiosupporter.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.takusan23.newradiosupporter.tool.data.LogcatPhysicalChannelConfigResult

@Composable
fun LogcatPhysicalChannelConfigInfo(
    modifier: Modifier = Modifier,
    result: LogcatPhysicalChannelConfigResult,
    isExpanded: Boolean
) {
    Column(modifier) {

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

            val primaryCell = when (result) {
                is LogcatPhysicalChannelConfigResult.Endc -> result.primaryCell
                is LogcatPhysicalChannelConfigResult.NrCa -> result.primaryCell
            }
            val secondaryCellList = when (result) {
                is LogcatPhysicalChannelConfigResult.Endc -> listOf(result.secondaryCell)
                is LogcatPhysicalChannelConfigResult.NrCa -> result.secondaryCellList
            }

            BandChip(
                borderColor = MaterialTheme.colorScheme.primary,
                band = primaryCell.band
            )
            secondaryCellList.forEach { cell ->
                BandChip(
                    borderColor = MaterialTheme.colorScheme.secondary,
                    band = cell.band
                )
            }
        }
    }
}

@Composable
private fun BandChip(
    modifier: Modifier = Modifier,
    borderColor: Color,
    band: String
) {
    Surface(
        modifier = modifier,
        border = BorderStroke(2.dp, borderColor),
        shape = RoundedCornerShape(5.dp),
        color = Color.Transparent,
        contentColor = borderColor
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            text = band
        )
    }
}