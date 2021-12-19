package io.github.takusan23.newradiosupporter.ui.component

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import io.github.takusan23.newradiosupporter.R
import io.github.takusan23.newradiosupporter.tool.BandData

@Composable
fun BandItem(
    modifier: Modifier = Modifier,
    bandData: BandData,
) {
    Surface(modifier = modifier) {
        CommonItem(
            icon = painterResource(id = if (bandData.isNR) R.drawable.ic_outline_5g_24 else R.drawable.ic_outline_4g_mobiledata_24),
            title = """
                キャリア名：${bandData.carrierName}
                接続中バンド：${bandData.band}
                NRARFCN(EARFCN)：${bandData.earfcn}
            """.trimIndent(),
            description = "バンド情報"
        )
    }

}