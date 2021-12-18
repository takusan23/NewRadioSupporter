package io.github.takusan23.newradiosupporter.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.takusan23.newradiosupporter.tool.NetworkCallback
import io.github.takusan23.newradiosupporter.ui.component.BandInfo
import io.github.takusan23.newradiosupporter.ui.component.NRTypeEmptyItem
import io.github.takusan23.newradiosupporter.ui.component.NRTypeItem
import io.github.takusan23.newradiosupporter.ui.component.UnlimitedInfo

/** ホーム画面 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current

    // Flowで収集する
    val isUnlimitedNetwork = NetworkCallback.listenUnlimitedNetwork(context).collectAsState(initial = null)
    val bandData = NetworkCallback.listenBand(context).collectAsState(initial = null)
    val newRadioType = NetworkCallback.listenNewRadioStatus(context).collectAsState(initial = null)

    Scaffold(
        topBar = {
            MediumTopAppBar(title = {
                Text(text = "5G (NR) 情報")
            })
        },
        content = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = {
                    item {
                        if (bandData.value != null) {
                            BandInfo(bandData = bandData.value!!)
                        }
                    }
                    item {
                        if (newRadioType.value != null) {
                            NRTypeItem(nrType = newRadioType.value!!)
                        } else {
                            NRTypeEmptyItem()
                        }
                    }
                    item {
                        if (isUnlimitedNetwork.value != null) {
                            UnlimitedInfo(isUnlimited = isUnlimitedNetwork.value!!)
                        }
                    }
                }
            )
        }
    )
}