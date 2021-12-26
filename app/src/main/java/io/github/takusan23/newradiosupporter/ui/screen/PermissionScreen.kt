package io.github.takusan23.newradiosupporter.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.takusan23.newradiosupporter.R

/**
 * 権限下さい画面
 *
 * @param onGranted 権限もらえたら呼ばれる
 * */
@Composable
fun PermissionScreen(
    onGranted: () -> Unit,
) {

    // 権限コールバック
    val permissionRequest = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions(), onResult = { resultList ->
        if (resultList.all { it.value }) {
            onGranted()
        }
    })

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(id = R.string.request_permission))
        Button(
            modifier = Modifier.padding(10.dp),
            onClick = { permissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE)) },
            content = { Text(text = stringResource(id = R.string.request_permission_button)) }
        )
    }

}