package io.github.takusan23.newradiosupporter.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.takusan23.newradiosupporter.BackgroundNRSupporter
import io.github.takusan23.newradiosupporter.R
import io.github.takusan23.newradiosupporter.tool.NetworkCallbackTool
import io.github.takusan23.newradiosupporter.ui.component.*

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
    val isUnlimitedNetwork = remember { NetworkCallbackTool.listenUnlimitedNetwork(context) }.collectAsState(initial = null)
    val multipleNetworkStatusList = remember { NetworkCallbackTool.listenMultipleSimNetworkStatus(context) }.collectAsState(initial = emptyList())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = { AboutMenuIcon { onNavigate(NavigationLinkList.SettingScreen) } },
                colors = TopAppBarDefaults.mediumTopAppBarColors(scrolledContainerColor = MaterialTheme.colorScheme.surface),
                scrollBehavior = scrollBehavior
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.padding(it),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // SIMカードの数だけ Flow を監視する
            itemsIndexed(multipleNetworkStatusList.value) { _, statusFlow ->
                val status = statusFlow.collectAsState(initial = null)
                // 押したら展開できるようにするため
                val isExpanded = remember { mutableStateOf(false) }
                // 初期値はデータ通信に設定されたSIMカードのスロット番号
                LaunchedEffect(key1 = status.value?.simSlotIndex) {
                    isExpanded.value = status.value?.simSlotIndex == NetworkCallbackTool.getDataUsageSimSlotIndex(context)
                }

                if (status.value != null) {
                    Card(
                        modifier = Modifier.padding(top = 20.dp, start = 20.dp, end = 20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(CardTonalElevation)),
                        onClick = { isExpanded.value = !isExpanded.value }
                    ) {
                        // 押したら展開できるように
                        if (isExpanded.value) {
                            SimNetWorkStatusExpanded(
                                modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
                                finalNRType = status.value?.finalNRType!!,
                                nrStandAloneType = status.value?.nrStandAloneType
                            )
                            BandItem(
                                modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
                                bandData = status.value?.bandData!!
                            )
                        } else {
                            SimNetworkOverview(
                                simIndex = status.value?.simSlotIndex!! + 1,
                                bandData = status.value?.bandData!!,
                                finalNRType = status.value?.finalNRType!!,
                                nrStandAloneType = status.value?.nrStandAloneType!!,
                            )
                        }
                    }
                }
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
                BackgroundServiceItem(
                    modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
                    onClick = {
                        if (BackgroundNRSupporter.isServiceRunning(context)) {
                            BackgroundNRSupporter.stopService(context)
                        } else {
                            BackgroundNRSupporter.startService(context)
                        }
                    }
                )
            }
        }
    }
}