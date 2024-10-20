package io.github.takusan23.newradiosupporter.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable

/** WindowInsets まとめ */
object WindowInsetsTool {

    /** Scaffold につける WindowInsets。自撮りカメラの部分は padding になる */
    val ScaffoldWindowInsets: WindowInsets
        @Composable
        get() = ScaffoldDefaults.contentWindowInsets
            .add(WindowInsets.displayCutout)

}