package io.github.takusan23.newradiosupporter.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.takusan23.newradiosupporter.BackgroundNRSupporter
import io.github.takusan23.newradiosupporter.R
import io.github.takusan23.newradiosupporter.tool.NetworkCallback
import io.github.takusan23.newradiosupporter.ui.component.BackgroundServiceItem
import io.github.takusan23.newradiosupporter.ui.component.BandItem
import io.github.takusan23.newradiosupporter.ui.component.TopInfo
import io.github.takusan23.newradiosupporter.ui.component.UnlimitedInfo

/** ホーム画面 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current

    // Flowで収集する
    val isUnlimitedNetwork = NetworkCallback.listenUnlimitedNetwork(context).collectAsState(initial = null)
    val bandData = NetworkCallback.listenBand(context).collectAsState(initial = null)
    val networkType = NetworkCallback.listenNetworkStatus(context).collectAsState(initial = null)

    val isRunningService = remember { mutableStateOf(BackgroundNRSupporter.isServiceRunning(context)) }
    // サービス起動
    LaunchedEffect(key1 = isRunningService.value, block = {
        if (isRunningService.value) {
            BackgroundNRSupporter.startService(context)
        } else {
            BackgroundNRSupporter.stopService(context)
        }
    })

    Scaffold(
        topBar = {
            MediumTopAppBar(title = {
                Text(text = stringResource(id = R.string.app_name))
            })
        },
        content = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = {
                    item {
                        if (bandData.value != null && networkType.value != null) {
                            TopInfo(
                                modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
                                finalNRType = NetworkCallback.finalResult(bandData.value!!, networkType.value!!)
                            )
                        }
                    }
                    item {
                        if (bandData.value != null) {
                            BandItem(
                                modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
                                bandData = bandData.value!!
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
                            isRunning = isRunningService.value,
                            onChecked = { isRunningService.value = it }
                        )
                    }
                }
            )
        }
    )
}