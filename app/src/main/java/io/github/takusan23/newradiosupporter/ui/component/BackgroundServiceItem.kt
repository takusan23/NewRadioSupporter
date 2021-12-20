package io.github.takusan23.newradiosupporter.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.takusan23.newradiosupporter.R

@Composable
fun BackgroundServiceItem(
    modifier: Modifier = Modifier,
    isRunning: Boolean,
    onChecked: (Boolean) -> Unit,
) {
    Surface(
        modifier = modifier,
        color = animateColorAsState(targetValue = if (isRunning) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface).value,
        shape = RoundedCornerShape(10.dp),
        interactionSource = remember { MutableInteractionSource() },
        indication = rememberRipple(),
        onClick = { onChecked(!isRunning) }
    ) {
        CommonItem(
            icon = painterResource(id = R.drawable.ic_outline_mark_chat_unread_24),
            title = if (isRunning) "実行中です" else "実行していません",
            description = "バックグラウンドで5G接続をお知らせする",
            endContent = {
                Checkbox(
                    modifier = Modifier.padding(5.dp),
                    checked = isRunning,
                    onCheckedChange = onChecked
                )
            }
        )
    }
}