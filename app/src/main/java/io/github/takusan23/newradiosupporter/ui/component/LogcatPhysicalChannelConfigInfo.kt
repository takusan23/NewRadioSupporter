package io.github.takusan23.newradiosupporter.ui.component

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import io.github.takusan23.newradiosupporter.R
import io.github.takusan23.newradiosupporter.tool.data.BandData
import io.github.takusan23.newradiosupporter.tool.data.LogcatPhysicalChannelConfigResult

/** [LogcatPhysicalChannelConfigResult]を表示する */
@Composable
fun LogcatPhysicalChannelConfigInfo(
    modifier: Modifier = Modifier,
    result: LogcatPhysicalChannelConfigResult,
    isExpanded: Boolean
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        // 広げてない場合は Chips だけ
        if (!isExpanded) {
            NotExpandedInfo(result = result)
            return
        }

        // タイトルと chips
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.experiment_24px),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
            Text(
                text = when (result) {
                    is LogcatPhysicalChannelConfigResult.CarrierAggregation -> "(実験的) キャリアアグリゲーション情報"
                    is LogcatPhysicalChannelConfigResult.Endc -> "(実験的) アンカーバンド情報"
                },
                fontSize = 20.sp
            )
        }
        NotExpandedInfo(result = result)

        when (result) {
            // Endc の場合（アンカーバンド情報表示）
            is LogcatPhysicalChannelConfigResult.Endc -> EndcInfo(
                primaryCell = result.primaryCell
            )

            // 4G/5G キャリアアグリゲーション表示
            is LogcatPhysicalChannelConfigResult.CarrierAggregation -> CarrierAggregationInfo(
                primaryCell = result.primaryCell,
                secondaryCellList = result.secondaryCellList
            )
        }
    }
}

/** READ_LOGS 権限ください */
@Composable
fun LogcatPermissionCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val command = "adb shell pm grant io.github.takusan23.newradiosupporter android.permission.READ_LOGS"

    // すでに権限付与済みなら return
    val isGranted = remember { ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_LOGS) == PackageManager.PERMISSION_GRANTED }
    if (isGranted) return

    OutlinedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = """
                    キャリアアグリゲーション情報を表示するためには、権限を付与する必要があります。
                    ただし、端末によっては権限を付与しても正しく表示されない場合があります。

                    権限を利用して収集したデータは、キャリアアグリゲーション、アンカーバンド表示のために利用され、それ以外の目的では利用しません。
                    表示のための処理は、端末内で処理が完結します。外部へ送信されることはありません。
                    
                    この権限はパソコンを使って、以下の ADB コマンドを実行する必要があります。
                    またアプリ起動時に表示される、デバイスログへのアクセスを許可してください。                    
                    
                    なんだかよくわからないという場合や、不安な場合は、何もしないでください。
                    初回起動時に要求した権限で Sub6/mmWave/転用5G や NSA/SA 判定は利用可能です。                    
                    
                    $command
                """.trimIndent()
            )
            Button(onClick = {
                val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboardManager.setPrimaryClip(ClipData.newPlainText("command", command))
            }) { Text(text = "コマンドをコピーする") }
        }
    }
}

/** 短縮表示 */
@Composable
private fun NotExpandedInfo(
    modifier: Modifier = Modifier,
    result: LogcatPhysicalChannelConfigResult
) {
    val primaryCell = when (result) {
        is LogcatPhysicalChannelConfigResult.Endc -> result.primaryCell
        is LogcatPhysicalChannelConfigResult.CarrierAggregation -> result.primaryCell
    }
    val secondaryCellList = when (result) {
        is LogcatPhysicalChannelConfigResult.Endc -> listOf(result.secondaryCell)
        is LogcatPhysicalChannelConfigResult.CarrierAggregation -> result.secondaryCellList
    }

    Row(modifier = modifier) {

        Icon(
            modifier = Modifier.size(30.dp),
            painter = painterResource(R.drawable.android_new_radio_supporter_carrier_aggregation),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(10.dp))

        // 折り返せるように
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            BandChip(
                borderColor = MaterialTheme.colorScheme.primary,
                isNr = primaryCell.isNR,
                band = primaryCell.band
            )

            secondaryCellList.forEach { cell ->
                BandChip(
                    borderColor = MaterialTheme.colorScheme.secondary,
                    isNr = primaryCell.isNR,
                    band = cell.band
                )
            }
        }
    }
}

/** アンカーバンドを表示する */
@Composable
private fun EndcInfo(
    modifier: Modifier = Modifier,
    primaryCell: BandData
) {
    SectionBox(
        modifier = modifier,
        title = "アンカーバンド",
        color = MaterialTheme.colorScheme.primary
    ) {
        // ひと回り小さく
        BandItem(
            modifier = Modifier.scale(0.9f),
            bandData = primaryCell
        )
        // なにそれ説明
        Surface(
            color = Color.Transparent,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Text(
                modifier = Modifier.padding(10.dp),
                text = "NSA 方式の 5G は、単体では動けずアンカーとなる 4G が存在します。そのバンド情報です"
            )
        }
    }
}

/** キャリアアグリゲーションを表示 */
@Composable
private fun CarrierAggregationInfo(
    modifier: Modifier = Modifier,
    primaryCell: BandData,
    secondaryCellList: List<BandData>
) {
    val nameList = listOf("RAT", "バンド", "NR-ARFCN")

    /** [BandData.isNR]なら 5G、そうじゃないなら 4G を返す */
    fun BandData.rat(): String = if (isNR) "5G" else "4G"

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        // プライマリーセル
        SectionBox(
            title = "プライマリーセル",
            color = MaterialTheme.colorScheme.primary
        ) {
            Table(
                nameList = nameList,
                column = listOf(
                    listOf(
                        primaryCell.rat(),
                        primaryCell.band,
                        primaryCell.earfcn.toString()
                    )
                )
            )
        }

        // セカンダリーセル
        SectionBox(
            title = "セカンダリーセル",
            color = MaterialTheme.colorScheme.secondary
        ) {
            Table(
                nameList = nameList,
                column = secondaryCellList.map { secondary ->
                    listOf(
                        secondary.rat(),
                        secondary.band,
                        secondary.earfcn.toString()
                    )
                }
            )
        }
    }
}

/** 縦棒を描画している */
@Composable
private fun SectionBox(
    modifier: Modifier = Modifier,
    title: String,
    color: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Max), // content() の高さに合わせる
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        // 縦棒
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(5.dp)
                .clip(CircleShape)
                .background(color)
        )

        // タイトルと content()
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(5.dp))

            content()
        }
    }
}

/**
 * テーブル
 *
 * column は以下の形式
 * [
 *   ["5G", "n78", "643334"]
 * ]
 *
 * @param nameList 名前
 * @param column テーブルの中身
 */
@Composable
private fun Table(
    modifier: Modifier = Modifier,
    nameList: List<String>,
    column: List<List<String>>
) {

    @Composable
    fun TableColumn(
        modifier: Modifier = Modifier,
        row: List<String>,
        isBold: Boolean = false
    ) {
        Row(modifier = modifier) {
            row.forEach { text ->
                Text(
                    modifier = Modifier.weight(1f),
                    text = text,
                    fontWeight = if (isBold) FontWeight.Bold else null,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }

    Column(modifier = modifier) {
        // 名前
        TableColumn(
            row = nameList,
            isBold = true
        )
        HorizontalDivider()

        // データ
        column.forEach { row -> TableColumn(row = row) }
    }
}

/** バンド表示の Chips */
@Composable
private fun BandChip(
    modifier: Modifier = Modifier,
    borderColor: Color,
    isNr: Boolean,
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
            text = if (isNr) "n$band" else "b$band"
        )
    }
}