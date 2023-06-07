package io.github.takusan23.newradiosupporter.ui.tool

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat

/**
 * ステータスバーの裏側に描画する
 *
 * @see [WindowCompat.setDecorFitsSystemWindows]
 */
@Composable
fun SetFitsSystemWindow() {
    val context = LocalContext.current
    LaunchedEffect(key1 = Unit) {
        val window = (context as Activity).window
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}
