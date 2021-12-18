package io.github.takusan23.newradiosupporter.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.takusan23.newradiosupporter.R
import io.github.takusan23.newradiosupporter.tool.BandData

@Composable
fun BandInfo(bandData: BandData) {
    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(50.dp),
            painter = painterResource(id = if (bandData.isNR) R.drawable.ic_outline_5g_24 else R.drawable.ic_outline_4g_mobiledata_24),
            contentDescription = null
        )
        Text(
            modifier = Modifier.padding(top = 10.dp),
            fontSize = 25.sp,
            text = bandData.carrierName
        )
        Text(
            text = "バンド : ${bandData.band}\n(${bandData.earfcn})",
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
    }
}