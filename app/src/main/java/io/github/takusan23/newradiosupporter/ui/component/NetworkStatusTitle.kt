package io.github.takusan23.newradiosupporter.ui.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.takusan23.newradiosupporter.R
import io.github.takusan23.newradiosupporter.tool.data.NetworkStatusData

/**
 * 見出し
 *
 * @param modifier [Modifier]
 * @param simInfo 物理SIM or eSIM
 * @param carrierName キャリア名
 * @param isExpanded Card を展開してるか
 * @param onClick 押したとき
 */
@Composable
fun NetworkStatusTitle(
    modifier: Modifier = Modifier,
    simInfo: NetworkStatusData.SimInfo,
    carrierName: String,
    isExpanded: Boolean,
    onClick: (Boolean) -> Unit
) {
    TextButton(
        modifier = modifier.padding(horizontal = 20.dp),
        onClick = { onClick(!isExpanded) }
    ) {
        Icon(
            painter = painterResource(
                id = when (simInfo) {
                    is NetworkStatusData.SimInfo.Esim -> R.drawable.sim_card_download_24px
                    is NetworkStatusData.SimInfo.PhysicalSim -> R.drawable.sim_card_24px
                }
            ),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))

        // slotIndex、0 始まりなので
        if (simInfo is NetworkStatusData.SimInfo.PhysicalSim) {
            Text(text = "${simInfo.simSlotIndex + 1} - ")
        }
        Text(text = carrierName)

        Spacer(modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(id = if (isExpanded) R.drawable.ic_expand_less_24 else R.drawable.ic_expand_more_24),
            contentDescription = null
        )
    }

}