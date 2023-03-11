package io.github.takusan23.newradiosupporter.ui.component

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.takusan23.newradiosupporter.R
import io.github.takusan23.newradiosupporter.tool.data.PhysicalChannelConfigData

private val ParentCardTonalElevation = 5.dp

/**
 * [android.telephony.PhysicalChannelConfig]を Shizuku-API を利用することで取得できるので、表示するためのUI
 */
@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperUserInfoCard(
    modifier: Modifier = Modifier,
    simIndex: Int,
    carrierName: String,
    primaryCell: List<PhysicalChannelConfigData>,
    secondaryCellList: List<PhysicalChannelConfigData>,
) {
    val isExpanded = remember { mutableStateOf(true) }

    val carrierAggregationBandList = remember(primaryCell, secondaryCellList) {
        (primaryCell.map { it.bandData?.band } + secondaryCellList.map { it.bandData?.band }).filterNotNull()
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(ParentCardTonalElevation)),
        onClick = { isExpanded.value = !isExpanded.value }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row {
                Text(
                    modifier = Modifier.weight(1f),
                    text = "SIM $simIndex - $carrierName"
                )
                Icon(
                    painter = painterResource(id = if (isExpanded.value) R.drawable.ic_expand_less_24 else R.drawable.ic_expand_more_24),
                    contentDescription = null
                )
            }

            Row {
                SuperUserOverview(
                    modifier = Modifier.fillMaxWidth(),
                    carrierAggregationBandList = carrierAggregationBandList,
                )
            }

            if (isExpanded.value) {
                SuperUserCellInfoCard(
                    modifier = Modifier.padding(top = 10.dp),
                    primaryCell = primaryCell,
                    secondaryCellList = secondaryCellList
                )
            }
        }
    }
}

@Composable
private fun SuperUserOverview(
    modifier: Modifier = Modifier,
    carrierAggregationBandList: List<String>,
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Max)
    ) {
        Text(
            modifier = Modifier
                .padding(10.dp)
                .weight(1f),
            text = """
                プライマリ/セカンダリ セル構成
                [ ${carrierAggregationBandList.joinToString(" + ")} ]
                """.trimIndent()
        )
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun SuperUserCellInfoCard(
    modifier: Modifier = Modifier,
    primaryCell: List<PhysicalChannelConfigData>,
    secondaryCellList: List<PhysicalChannelConfigData>,
) {
    Column(modifier = modifier) {
        SuperUserCellInfoItem(
            borderColor = MaterialTheme.colorScheme.primary,
            cellList = primaryCell
        )
        SuperUserCellInfoItem(
            modifier = Modifier.padding(top = 5.dp),
            borderColor = MaterialTheme.colorScheme.secondary,
            cellList = secondaryCellList
        )
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun SuperUserCellInfoItem(
    modifier: Modifier = Modifier,
    borderColor: Color,
    cellList: List<PhysicalChannelConfigData>
) {

    Row(
        modifier = modifier
            .height(IntrinsicSize.Max)
    ) {
        VerticalDivider(
            modifier = Modifier.padding(5.dp),
            width = 5.dp,
            color = borderColor,
            shape = RoundedCornerShape(5.dp)
        )
        Column {
            cellList.forEach { config ->
                val cellType = when (config.cellType) {
                    PhysicalChannelConfigData.CellType.PRIMARY -> "プライマリーセル"
                    PhysicalChannelConfigData.CellType.SECONDARY -> "セカンダリーセル"
                    else -> "不明"
                }
                val networkType = when (config.networkType) {
                    PhysicalChannelConfigData.NetworkType.NR -> "5G"
                    PhysicalChannelConfigData.NetworkType.LTE -> "4G"
                    PhysicalChannelConfigData.NetworkType.LTE_CA -> "4G CA"
                }
                Text(
                    modifier = Modifier.padding(bottom = 5.dp),
                    text = """
                    $cellType ($networkType)
                    バンド : ${config.bandData?.band} | 周波数 : ${config.bandData?.frequencyMHz ?: -1} MHz
                    NRARFCN (EARFCN) : ${config.bandData?.earfcn}
                """.trimIndent()
                )
            }
        }
    }
}