package io.github.takusan23.newradiosupporter.ui.component

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import io.github.takusan23.newradiosupporter.R

/** メニューアイコン */
@Composable
fun AboutMenuIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
    ) { Icon(painter = painterResource(id = R.drawable.ic_outline_more_vert_24), contentDescription = null) }
}

/** 戻るボタン */
@Composable
fun BackIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
    ) { Icon(painter = painterResource(id = R.drawable.ic_outline_arrow_back_24), contentDescription = null) }
}