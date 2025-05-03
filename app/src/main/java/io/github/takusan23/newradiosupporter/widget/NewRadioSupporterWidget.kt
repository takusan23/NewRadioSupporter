package io.github.takusan23.newradiosupporter.widget

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import io.github.takusan23.newradiosupporter.MainActivity
import io.github.takusan23.newradiosupporter.R
import io.github.takusan23.newradiosupporter.tool.NetworkStatusFlow
import io.github.takusan23.newradiosupporter.tool.data.FinalNrType
import io.github.takusan23.newradiosupporter.tool.data.NetworkStatusData
import io.github.takusan23.newradiosupporter.tool.data.NrStandAloneType
import io.github.takusan23.newradiosupporter.ui.theme.DarkThemeColors
import io.github.takusan23.newradiosupporter.ui.theme.LightThemeColors
import kotlinx.coroutines.launch

/**
 * Jetpack Compose の書き方で RemoteView が作れる
 * 最終的には RemoteView になるので、androidx.glance 用の関数を呼び出す必要がある
 */
class NewRadioSupporterWidget : GlanceAppWidget() {

    /** ウィジェットの利用可能なサイズ。通常と横に長いサイズ */
    override val sizeMode = SizeMode.Responsive(setOf(SMALL, LARGE))

    /** ウィジェットのレイアウトをここに書いていく */
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val scope = rememberCoroutineScope()

            /** ウィジェットを更新する */
            fun update() {
                scope.launch {
                    NewRadioSupporterWidget().update(context, id)
                }
            }

            // Material You が使える場合は使う
            GlanceTheme(colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) GlanceTheme.colors else colors) {
                val size = LocalSize.current
                // サイズによってレイアウトを切り替える
                if (size.width >= LARGE.width) {
                    LargeWidgetContent(
                        context = context,
                        onUpdateButtonClick = { update() }
                    )
                } else {
                    SmallWidgetContent(
                        context = context,
                        onUpdateButtonClick = { update() }
                    )
                }
            }
        }
    }

    /**
     * 小さいウィジェットのレイアウト
     *
     * @param context [Context]
     * @param onUpdateButtonClick 更新ボタンを押したら呼ばれる
     */
    @Composable
    private fun SmallWidgetContent(
        context: Context,
        onUpdateButtonClick: () -> Unit
    ) {
        val multipleNetworkStatusDataList = NetworkStatusFlow.collectMultipleNetworkStatus(context).collectAsState(initial = emptyList())

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.secondaryContainer) // 他のウィジェットもこの色っぽい
                .cornerRadius(16.dp)
        ) {
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                // 更新、起動ボタン
                item {
                    WidgetButtonRow(
                        modifier = GlanceModifier.fillMaxWidth(),
                        onUpdateButtonClick = onUpdateButtonClick
                    )
                }
                // 縦並びで情報の表示
                if (multipleNetworkStatusDataList.value.isEmpty()) {
                    // 取得できなかった時
                    item {
                        Box(modifier = GlanceModifier.padding(5.dp)) {
                            EmptyState(context = context)
                        }
                    }
                } else {
                    // SIM カードの枚数分表示
                    items(multipleNetworkStatusDataList.value) { status ->
                        Box(modifier = GlanceModifier.padding(5.dp)) {
                            SmallNetworkStatusCard(networkStatusData = status)
                        }
                    }
                }
            }
        }
    }

    /**
     * 大きいウィジェットのレイアウト
     *
     * @param context [Context]
     * @param onUpdateButtonClick 更新ボタンを押したら呼ばれる
     */
    @Composable
    private fun LargeWidgetContent(
        context: Context,
        onUpdateButtonClick: () -> Unit
    ) {
        val multipleNetworkStatusDataList = NetworkStatusFlow.collectMultipleNetworkStatus(context).collectAsState(initial = emptyList())

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.secondaryContainer)
                .cornerRadius(16.dp)
        ) {
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                // 更新、起動ボタン
                item {
                    WidgetButtonRow(
                        modifier = GlanceModifier.fillMaxWidth(),
                        onUpdateButtonClick = onUpdateButtonClick
                    )
                }
                // 横並びで情報の表示
                item {
                    Row(
                        modifier = GlanceModifier
                            .padding(5.dp)
                            .fillMaxWidth()
                    ) {
                        if (multipleNetworkStatusDataList.value.isEmpty()) {
                            // 取得できなかった時
                            EmptyState(
                                modifier = GlanceModifier.defaultWeight(),
                                context = context
                            )
                        } else {
                            // SIM カードの枚数分表示
                            multipleNetworkStatusDataList.value.forEachIndexed { index, status ->
                                // デュアルSIMの場合は間にスペースを開ける
                                if (index != 0) {
                                    Spacer(modifier = GlanceModifier.size(5.dp))
                                }
                                LargeNetworkStatusCard(
                                    modifier = GlanceModifier.defaultWeight(),
                                    context = context,
                                    networkStatusData = status
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 大きい版 5G の状態を表示する
     *
     * @param modifier [GlanceModifier]
     * @param networkStatusData [NetworkStatusData]
     */
    @Composable
    private fun LargeNetworkStatusCard(
        modifier: GlanceModifier = GlanceModifier,
        context: Context,
        networkStatusData: NetworkStatusData
    ) {
        Column(
            modifier = modifier
                .background(GlanceTheme.colors.background)
                .cornerRadius(10.dp)
                .padding(5.dp)
        ) {

            val nrStandAloneTypeText = when (networkStatusData.nrStandAloneType) {
                NrStandAloneType.STAND_ALONE -> context.getString(R.string.type_stand_alone_5g_short)
                NrStandAloneType.NON_STAND_ALONE -> context.getString(R.string.type_non_stand_alone_5g_short)
                NrStandAloneType.ERROR -> context.getString(R.string.type_4g_short)
            }
            NetworkStatusCardTitle(
                modifier = GlanceModifier.fillMaxWidth(),
                simInfo = networkStatusData.simInfo,
                carrierName = networkStatusData.bandData.carrierName,
                chipText = "${networkStatusData.bandData.band} / $nrStandAloneTypeText"
            )

            Spacer(modifier = GlanceModifier.height(5.dp))

            NrStatusIcons(
                finalNrType = networkStatusData.finalNRType,
                standAloneType = networkStatusData.nrStandAloneType
            )

            Spacer(modifier = GlanceModifier.height(5.dp))

            Text(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(colorProvider = if (networkStatusData.finalNRType.isNr) GlanceTheme.colors.secondaryContainer else GlanceTheme.colors.errorContainer)
                    .cornerRadius(5.dp)
                    .padding(10.dp),
                text = when (networkStatusData.finalNRType) {
                    FinalNrType.NR_MMW -> context.getString(R.string.type_nr_mmwave)
                    FinalNrType.NR_SUB6 -> context.getString(R.string.type_nr_sub6)
                    FinalNrType.NR_LTE_FREQUENCY -> context.getString(R.string.type_lte_freq_nr)
                    FinalNrType.MAYBE_NR -> context.getString(R.string.type_maybe_nr)
                    FinalNrType.ANCHOR_BAND -> context.getString(R.string.type_lte_anchor_band)
                    FinalNrType.LTE -> context.getString(R.string.type_lte)
                    else -> context.getString(R.string.loading)
                },
                style = TextStyle(color = if (networkStatusData.finalNRType.isNr) GlanceTheme.colors.secondary else GlanceTheme.colors.error)
            )

            Spacer(modifier = GlanceModifier.height(5.dp))

            Text(
                modifier = GlanceModifier.padding(5.dp),
                text = """
                    ${context.getString(R.string.earfcn)}
                    ${networkStatusData.bandData.earfcn}
                """.trimIndent(),
                style = TextStyle(color = GlanceTheme.colors.primary)
            )

            Text(
                modifier = GlanceModifier.padding(5.dp),
                text = """
                    ${context.getString(R.string.frequency)}
                    ${networkStatusData.bandData.frequencyMHz} MHz
                """.trimIndent(),
                style = TextStyle(color = GlanceTheme.colors.primary)
            )
        }
    }

    /**
     * 小さい版 5G の状態を表示する
     * ウィジェットのリストの中身のレイアウトです
     *
     * @param modifier [GlanceModifier]
     * @param networkStatusData [NetworkStatusData]
     */
    @Composable
    private fun SmallNetworkStatusCard(
        modifier: GlanceModifier = GlanceModifier,
        networkStatusData: NetworkStatusData
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(GlanceTheme.colors.background)
                .cornerRadius(10.dp)
                .padding(5.dp),
        ) {

            NetworkStatusCardTitle(
                modifier = GlanceModifier.fillMaxWidth(),
                simInfo = networkStatusData.simInfo,
                carrierName = networkStatusData.bandData.carrierName,
                chipText = networkStatusData.bandData.band
            )

            NrStatusIcons(
                modifier = GlanceModifier.padding(top = 5.dp),
                finalNrType = networkStatusData.finalNRType,
                standAloneType = networkStatusData.nrStandAloneType
            )
        }
    }

    /**
     * 5G のアイコンを出す（周波数・SA/NSA）
     *
     * @param modifier [GlanceModifier]
     * @param finalNrType [FinalNrType]
     * @param standAloneType [NrStandAloneType]
     */
    @Composable
    private fun NrStatusIcons(
        modifier: GlanceModifier = GlanceModifier,
        finalNrType: FinalNrType,
        standAloneType: NrStandAloneType
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {

            Image(
                modifier = GlanceModifier
                    .fillMaxHeight()
                    .defaultWeight(),
                provider = ImageProvider(
                    resId = when (finalNrType) {
                        FinalNrType.ANCHOR_BAND -> R.drawable.ic_android_anchor_lte_band
                        FinalNrType.NR_LTE_FREQUENCY -> R.drawable.android_nr_lte_freq_nr
                        FinalNrType.NR_SUB6 -> R.drawable.ic_android_nr_sub6
                        FinalNrType.NR_MMW -> R.drawable.ic_android_nr_mmw
                        FinalNrType.LTE -> R.drawable.ic_android_lte
                        else -> R.drawable.ic_outline_info_24
                    }
                ),
                contentDescription = null,
                colorFilter = ColorFilter.tint(GlanceTheme.colors.primary)
            )

            Image(
                modifier = GlanceModifier
                    .fillMaxHeight()
                    .defaultWeight(),
                provider = ImageProvider(
                    resId = when (standAloneType) {
                        NrStandAloneType.STAND_ALONE -> R.drawable.android_5g_stand_alone
                        NrStandAloneType.NON_STAND_ALONE -> R.drawable.android_5g_non_stand_alone
                        NrStandAloneType.ERROR -> R.drawable.ic_outline_4g_mobiledata_24
                    }
                ),
                contentDescription = null,
                colorFilter = ColorFilter.tint(GlanceTheme.colors.primary)
            )
        }
    }

    /**
     * エンプティーステート
     * 更新ボタンを押してくださいのUI
     *
     * @param modifier [GlanceModifier]
     * @param context [Context]
     */
    @Composable
    private fun EmptyState(
        modifier: GlanceModifier = GlanceModifier,
        context: Context
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(GlanceTheme.colors.background)
                .cornerRadius(10.dp)
                .padding(5.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Image(
                modifier = GlanceModifier.size(40.dp),
                provider = ImageProvider(R.drawable.ic_outline_info_24),
                contentDescription = null,
                colorFilter = ColorFilter.tint(GlanceTheme.colors.primary)
            )
            Text(
                text = context.getString(R.string.widget_update_message),
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    textAlign = TextAlign.Center
                )
            )
        }
    }

    /**
     * カード内のタイトル部分。キャリア名が出ているあれ
     *
     * @param modifier [GlanceModifier]
     * @param simInfo 物理SIM or eSIM
     * @param carrierName キャリア名
     * @param chipText バンド表示してる部分
     */
    @Composable
    private fun NetworkStatusCardTitle(
        modifier: GlanceModifier = GlanceModifier,
        simInfo: NetworkStatusData.SimInfo,
        carrierName: String,
        chipText: String
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {

            Image(
                modifier = GlanceModifier.size(20.dp),
                provider = ImageProvider(
                    resId = when (simInfo) {
                        is NetworkStatusData.SimInfo.Esim -> R.drawable.sim_card_download_24px
                        is NetworkStatusData.SimInfo.PhysicalSim -> R.drawable.sim_card_24px
                    }
                ),
                contentDescription = null,
                colorFilter = ColorFilter.tint(GlanceTheme.colors.primary)
            )

            if (simInfo is NetworkStatusData.SimInfo.PhysicalSim) {
                Text(
                    text = "${simInfo.simSlotIndex + 1} - ",
                    style = TextStyle(color = GlanceTheme.colors.primary)
                )
            }
            Text(
                modifier = GlanceModifier.defaultWeight(),
                text = carrierName,
                style = TextStyle(color = GlanceTheme.colors.primary)
            )

            ChipText(text = chipText)
        }
    }

    /**
     * チップ？のテキスト
     *
     * @param modifier [GlanceModifier]
     * @param text テキスト
     */
    @Composable
    private fun ChipText(
        modifier: GlanceModifier = GlanceModifier,
        text: String
    ) {
        Text(
            modifier = modifier
                .padding(5.dp)
                .background(GlanceTheme.colors.tertiaryContainer)
                .cornerRadius(10.dp),
            text = text,
            style = TextStyle(color = GlanceTheme.colors.tertiary)
        )
    }

    /**
     * ウィジェットのボタンをまとめたもの
     *
     * @param modifier [GlanceModifier]
     * @param onUpdateButtonClick 更新ボタンを押した時に呼ばれる
     */
    @Composable
    private fun WidgetButtonRow(
        modifier: GlanceModifier = GlanceModifier,
        onUpdateButtonClick: () -> Unit
    ) {
        Row(
            modifier = modifier
                .padding(5.dp),
            horizontalAlignment = Alignment.Horizontal.End
        ) {
            WidgetLaunchButton()
            Spacer(modifier = GlanceModifier.width(10.dp))
            WidgetUpdateButton(onClick = onUpdateButtonClick)
        }
    }

    /**
     * 更新ボタン
     *
     * @param modifier [GlanceModifier]
     * @param onClick 押したら呼ばれます
     */
    @Composable
    private fun WidgetUpdateButton(
        modifier: GlanceModifier = GlanceModifier,
        onClick: () -> Unit
    ) {
        Image(
            modifier = modifier
                .size(40.dp)
                .padding(5.dp)
                .background(GlanceTheme.colors.primary)
                .cornerRadius(10.dp)
                .clickable(onClick),
            provider = ImageProvider(resId = R.drawable.ic_outline_refresh_24),
            contentDescription = null,
            colorFilter = ColorFilter.tint(GlanceTheme.colors.primaryContainer)
        )
    }

    /**
     * アプリを起動するボタン
     *
     * @param modifier [GlanceModifier]
     */
    @Composable
    private fun WidgetLaunchButton(modifier: GlanceModifier = GlanceModifier) {
        Image(
            modifier = modifier
                .size(40.dp)
                .padding(10.dp)
                .cornerRadius(10.dp)
                .clickable(actionStartActivity(MainActivity::class.java)),
            provider = ImageProvider(resId = R.drawable.ic_outline_open_in_new_24),
            contentDescription = null,
            colorFilter = ColorFilter.tint(GlanceTheme.colors.primary)
        )
    }

    companion object {

        /** ウィジェットの色 */
        val colors = ColorProviders(
            light = LightThemeColors,
            dark = DarkThemeColors
        )

        /** 小さいサイズ */
        private val SMALL = DpSize(width = 100.dp, height = 100.dp)

        /** 大きいサイズ */
        private val LARGE = DpSize(width = 250.dp, height = 100.dp)
    }

}