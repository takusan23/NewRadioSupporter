package io.github.takusan23.newradiosupporter.widget

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
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
import androidx.glance.material3.ColorProviders
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import io.github.takusan23.newradiosupporter.R
import io.github.takusan23.newradiosupporter.tool.NetworkStatusFlow
import io.github.takusan23.newradiosupporter.tool.data.FinalNrType
import io.github.takusan23.newradiosupporter.tool.data.NetworkStatusData
import io.github.takusan23.newradiosupporter.tool.data.NrStandAloneType
import io.github.takusan23.newradiosupporter.ui.theme.DarkThemeColors
import io.github.takusan23.newradiosupporter.ui.theme.LightThemeColors

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
            // Material You が使える場合は使う
            GlanceTheme(colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) GlanceTheme.colors else colors) {
                val size = LocalSize.current
                // サイズによってレイアウトを切り替える
                if (size.width >= LARGE.width) {
                    LargeWidgetContent(context = context)
                } else {
                    SmallWidgetContent(context = context)
                }
            }
        }
    }

    /** 小さいウィジェットのレイアウト */
    @Composable
    private fun SmallWidgetContent(context: Context) {
        val multipleSimSubscriptionIdList = NetworkStatusFlow.collectMultipleSimSubscriptionIdList(context).collectAsState(emptyList())
        val updateKey = remember { mutableStateOf(System.currentTimeMillis()) }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .cornerRadius(16.dp),
        ) {

            Row(
                modifier = GlanceModifier
                    .padding(5.dp)
                    .fillMaxWidth()
            ) {
                Spacer(modifier = GlanceModifier.defaultWeight())
                WidgetUpdateButton {
                    updateKey.value = System.currentTimeMillis()
                }
            }

            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(multipleSimSubscriptionIdList.value) { subscriptionId ->
                    val status = remember { mutableStateOf<NetworkStatusData?>(null) }
                    // 電測データを Flow で収集する
                    // provideContent が動いている間は動くはず...
                    LaunchedEffect(key1 = updateKey.value) {
                        NetworkStatusFlow.collectNetworkStatus(context, subscriptionId).collect {
                            status.value = it
                        }
                    }
                    Box(modifier = GlanceModifier.padding(5.dp)) {
                        if (status.value != null) {
                            SmallNetworkStatusCard(networkStatusData = status.value!!)
                        }
                    }
                }
            }
        }
    }

    /** 大きいウィジェットのレイアウト */
    @Composable
    private fun LargeWidgetContent(context: Context) {
        val multipleSimSubscriptionIdList = NetworkStatusFlow.collectMultipleSimSubscriptionIdList(context).collectAsState(emptyList())
        val updateKey = remember { mutableStateOf(System.currentTimeMillis()) }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .cornerRadius(16.dp),
        ) {

            Row(
                modifier = GlanceModifier
                    .padding(5.dp)
                    .fillMaxWidth()
            ) {
                Spacer(modifier = GlanceModifier.defaultWeight())
                WidgetUpdateButton {
                    updateKey.value = System.currentTimeMillis()
                }
            }

            Row(modifier = GlanceModifier.fillMaxSize()) {
                multipleSimSubscriptionIdList.value.forEach { subscriptionId ->
                    val status = remember { mutableStateOf<NetworkStatusData?>(null) }
                    // 電測データを Flow で収集する
                    // provideContent が動いている間は動くはず...
                    LaunchedEffect(key1 = updateKey.value) {
                        NetworkStatusFlow.collectNetworkStatus(context, subscriptionId).collect {
                            status.value = it
                        }
                    }
                    Box(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .padding(5.dp)
                    ) {
                        if (status.value != null) {
                            LargeNetworkStatusCard(
                                context = context,
                                networkStatusData = status.value!!
                            )
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
                .fillMaxSize()
                .background(GlanceTheme.colors.primaryContainer)
                .cornerRadius(10.dp)
                .padding(5.dp),
        ) {

            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Text(
                    modifier = GlanceModifier.defaultWeight(),
                    text = networkStatusData.bandData.carrierName,
                    style = TextStyle(color = GlanceTheme.colors.primary)
                )

                val nrStandAloneTypeText = when (networkStatusData.nrStandAloneType) {
                    NrStandAloneType.STAND_ALONE -> context.getString(R.string.type_stand_alone_5g_short)
                    NrStandAloneType.NON_STAND_ALONE -> context.getString(R.string.type_non_stand_alone_5g_short)
                    NrStandAloneType.ERROR -> context.getString(R.string.type_4g_short)
                }
                ChipText(text = "${networkStatusData.bandData.band} / $nrStandAloneTypeText")
            }

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
                .background(GlanceTheme.colors.primaryContainer)
                .cornerRadius(10.dp)
                .padding(5.dp),
        ) {

            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Text(
                    modifier = GlanceModifier.defaultWeight(),
                    text = networkStatusData.bandData.carrierName,
                    style = TextStyle(color = GlanceTheme.colors.primary)
                )

                ChipText(text = networkStatusData.bandData.band)
            }

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
                colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
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
                colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
            )
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
                .background(GlanceTheme.colors.secondaryContainer)
                .cornerRadius(10.dp)
                .clickable(onClick),
            provider = ImageProvider(resId = R.drawable.ic_outline_info_24),
            contentDescription = null,
            colorFilter = ColorFilter.tint(GlanceTheme.colors.secondary),
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