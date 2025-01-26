package io.github.takusan23.newradiosupporter.ui.screen

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import rikka.shizuku.Shizuku

private const val SHIZUKU_PERMISSION_CODE = 2525

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShizukuPermissionScreen(onGranted: () -> Unit) {
    val lifecycle = LocalLifecycleOwner.current

    DisposableEffect(key1 = Unit) {
        val listener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                onGranted()
            }
        }
        val observer = object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)
                Shizuku.addRequestPermissionResultListener(listener)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                Shizuku.removeRequestPermissionResultListener(listener)
            }
        }
        lifecycle.lifecycle.addObserver(observer)
        onDispose { lifecycle.lifecycle.removeObserver(observer) }
    }

    Scaffold {
        Box(modifier = Modifier.padding(it)) {
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "「Shizuku」のセットアップが必要です。")
                Text(text = "セットアップが完了したら、このアプリに権限を付与してください。")
                Button(onClick = { Shizuku.requestPermission(SHIZUKU_PERMISSION_CODE) }) {
                    Text(text = "Shizukuの権限付与")
                }
            }
        }
    }

}
