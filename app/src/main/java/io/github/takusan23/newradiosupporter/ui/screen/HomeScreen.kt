package io.github.takusan23.newradiosupporter.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.takusan23.newradiosupporter.BackgroundNrSupporter
import io.github.takusan23.newradiosupporter.R
import io.github.takusan23.newradiosupporter.tool.NetworkStatusFlow
import io.github.takusan23.newradiosupporter.tool.PermissionCheckTool
import io.github.takusan23.newradiosupporter.tool.SettingIntentTool
import io.github.takusan23.newradiosupporter.tool.ShizukuTool
import io.github.takusan23.newradiosupporter.tool.data.PhysicalChannelConfigData
import io.github.takusan23.newradiosupporter.ui.WindowInsetsTool
import io.github.takusan23.newradiosupporter.ui.component.AboutMenuIcon
import io.github.takusan23.newradiosupporter.ui.component.BackgroundNrPermissionDialog
import io.github.takusan23.newradiosupporter.ui.component.BackgroundServiceItem
import io.github.takusan23.newradiosupporter.ui.component.BandItem
import io.github.takusan23.newradiosupporter.ui.component.OpenMobileNetworkSettingMenu
import io.github.takusan23.newradiosupporter.ui.component.SimNetWorkStatusExpanded
import io.github.takusan23.newradiosupporter.ui.component.SimNetworkOverview
import io.github.takusan23.newradiosupporter.ui.component.SuperUserInfoCard
import io.github.takusan23.newradiosupporter.ui.component.UnlimitedInfo

/** 回線状態を表示している Card の tonalElevation */
private val CardTonalElevation = 1.dp

/**
 * ホーム画面
 *
 * @param onNavigate 画面遷移を行う際に呼ばれる
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    // スクロールしたら AppBar を小さくするやつ
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    // Flowで収集する
    val isUnlimitedNetwork = NetworkStatusFlow.collectUnlimitedNetwork(context).collectAsStateWithLifecycle(initialValue = null)
    val multipleNetworkStatusDataList = NetworkStatusFlow.collectMultipleNetworkStatus(context).collectAsStateWithLifecycle(initialValue = emptyList())
    val multiplePhysicalChannelConfigDataList = ShizukuTool.collectMultiplePhysicalChannelConfigDataList(context).collectAsStateWithLifecycle(initialValue = emptyList())

    // バックグラウンドの権限ダイアログを出すか
    val isOpenBackgroundPermissionDialog = remember { mutableStateOf(false) }
    if (isOpenBackgroundPermissionDialog.value) {
        BackgroundNrPermissionDialog(
            onDismissRequest = { isOpenBackgroundPermissionDialog.value = false },
            onGranted = { BackgroundNrSupporter.toggleService(context) }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = { AboutMenuIcon { onNavigate(NavigationLinkList.SettingScreen) } },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsetsTool.ScaffoldWindowInsets
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // SIM カードの枚数分表示
            items(multipleNetworkStatusDataList.value) { status ->
                // 押したら展開できるようにするため
                // 初期値はデータ通信に設定されたSIMカードのスロット番号
                val isExpanded = remember { mutableStateOf(status.simSlotIndex == NetworkStatusFlow.getDataUsageSimSlotIndex(context)) }

                Card(
                    modifier = Modifier.padding(top = 20.dp, start = 20.dp, end = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(CardTonalElevation)),
                    onClick = { isExpanded.value = !isExpanded.value }
                ) {
                    // 押したら展開できるように
                    if (isExpanded.value) {
                        SimNetWorkStatusExpanded(
                            modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
                            finalNRType = status.finalNRType,
                            nrStandAloneType = status.nrStandAloneType
                        )
                        BandItem(
                            modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
                            bandData = status.bandData
                        )
                    } else {
                        SimNetworkOverview(
                            simIndex = status.simSlotIndex + 1,
                            bandData = status.bandData,
                            finalNRType = status.finalNRType,
                            nrStandAloneType = status.nrStandAloneType,
                        )
                    }
                }

            }

            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(0.5f)
                    )
                }
            }

            // 物理チャンネル構成
            items(multiplePhysicalChannelConfigDataList.value) { physicalChannelConfigDataList ->
                val primaryCell = physicalChannelConfigDataList.filter { data -> data.cellType == PhysicalChannelConfigData.CellType.PRIMARY }
                val secondaryCell = physicalChannelConfigDataList.filter { data -> data.cellType == PhysicalChannelConfigData.CellType.SECONDARY }

                SuperUserInfoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp),
                    simIndex = primaryCell.firstOrNull()?.simSlotIndex ?: 1,
                    carrierName = primaryCell.firstOrNull()?.bandData?.carrierName ?: "",
                    primaryCell = primaryCell,
                    secondaryCellList = secondaryCell
                )
            }

            item {
                if (isUnlimitedNetwork.value != null) {
                    UnlimitedInfo(
                        modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
                        isUnlimited = isUnlimitedNetwork.value!!
                    )
                }
            }
            item {
                OpenMobileNetworkSettingMenu(
                    modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
                    onClick = { SettingIntentTool.openMobileDataNetworkSetting(context) }
                )
            }
            item {
                BackgroundServiceItem(
                    modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
                    onClick = {
                        // 権限があれば起動
                        if (PermissionCheckTool.isGrantedNotificationPermission(context) && PermissionCheckTool.isGrantedBackgroundLocationPermission(context)) {
                            BackgroundNrSupporter.toggleService(context)
                        } else {
                            isOpenBackgroundPermissionDialog.value = true
                        }
                    }
                )
            }
        }
    }
}