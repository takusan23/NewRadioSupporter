package io.github.takusan23.newradiosupporter.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.takusan23.newradiosupporter.BackgroundNRSupporter
import io.github.takusan23.newradiosupporter.R
import io.github.takusan23.newradiosupporter.tool.NetworkCallbackTool
import io.github.takusan23.newradiosupporter.ui.component.*

/**
 * ホーム画面
 *
 * @param onNavigate 画面遷移を行う際に呼ばれる
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current

    // Flowで収集する
    val isUnlimitedNetwork = NetworkCallbackTool.listenUnlimitedNetwork(context).collectAsState(initial = null)
    val networkTypeFlow = NetworkCallbackTool.listenNetworkStatus(context).collectAsState(initial = null)

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = { AboutMenuIcon { onNavigate(NavigationLinkList.SettingScreen) } },
            )
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    if (networkTypeFlow.value?.second != null) {
                        TopInfo(
                            modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
                            finalNRType = networkTypeFlow.value?.second!!,
                            nrStandAloneType = networkTypeFlow.value?.third
                        )
                    }
                }
                item {
                    if (networkTypeFlow.value?.first != null) {
                        BandItem(
                            modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
                            bandData = networkTypeFlow.value?.first!!
                        )
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
}