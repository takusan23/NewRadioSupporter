package io.github.takusan23.newradiosupporter.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 縦方向の区切り線
 *
 * @param modifier [Modifier]
 */
@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    width: Dp = 1.dp,
    color: Color = LocalContentColor.current,
    shape: Shape = RoundedCornerShape(5.dp)
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(width)
            .background(color, shape)
    )
}