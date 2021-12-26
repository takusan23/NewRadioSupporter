package io.github.takusan23.newradiosupporter.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.takusan23.newradiosupporter.R
import io.github.takusan23.newradiosupporter.ui.component.BackIcon

/**
 * ライセンス画面
 *
 * @param onBack 戻るときに呼ばれる
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseScreen(onBack: () -> Unit) {
    val list = listOf(
        materialComponents,
        coroutine
    )
    Scaffold(
        topBar = {
            SmallTopAppBar(
                navigationIcon = { BackIcon(onClick = onBack) },
                title = { Text(text = stringResource(id = R.string.license)) }
            )
        },
        content = {
            LazyColumn(content = {
                items(list) { data ->
                    LicenseItem(licenseData = data)
                }
            })
        }
    )
}

/**
 * ライセンス一覧の各項目
 *
 * @param licenseData ライセンス情報
 * */
@Composable
private fun LicenseItem(licenseData: LicenseData) {
    Surface {
        Column {
            Text(
                modifier = Modifier.padding(5.dp),
                text = licenseData.name,
                fontSize = 25.sp
            )
            Text(
                text = licenseData.license,
                modifier = Modifier.padding(start = 5.dp, end = 5.dp)
            )
            Divider()
        }
    }
}

private val materialComponents = LicenseData(
    name = "material-components/material-components-android",
    license = """
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
    """.trimIndent()
)

private val coroutine = LicenseData(
    name = "Kotlin/kotlinx.coroutines",
    license = """
   Copyright 2000-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
    """.trimIndent()
)


/**
 * ライセンス情報データクラス
 * @param name 名前
 * @param license ライセンス
 * */
private data class LicenseData(
    val name: String,
    val license: String,
)