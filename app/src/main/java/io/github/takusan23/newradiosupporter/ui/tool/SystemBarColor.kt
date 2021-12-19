package io.github.takusan23.newradiosupporter.ui.tool

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

/**
 * ナビゲーションバーの色を変更する
 *
 * @param color 変更する色
 * */
@Composable
fun SetNavigationBarColor(color: Color = MaterialTheme.colorScheme.surface) {
    val context = LocalContext.current

    // ナビゲーションバーの色
    LaunchedEffect(key1 = color, block = {
        (context as? Activity)?.window?.navigationBarColor = android.graphics.Color.argb(
            color.toArgb().alpha,
            color.toArgb().red,
            color.toArgb().green,
            color.toArgb().blue,
        )
    })
}

/**
 * ステータスバーの色を設定する
 *
 * @param color 変更する色
 * */
@Composable
fun SetStatusBarColor(color: Color = MaterialTheme.colorScheme.surface) {
    val context = LocalContext.current

    // ナビゲーションバーの色
    LaunchedEffect(key1 = color, block = {
        (context as? Activity)?.window?.statusBarColor = android.graphics.Color.argb(
            color.toArgb().alpha,
            color.toArgb().red,
            color.toArgb().green,
            color.toArgb().blue,
        )
    })
}