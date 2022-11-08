package io.github.takusan23.newradiosupporter.ui.component

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.github.takusan23.newradiosupporter.R

@Composable
fun UnlimitedInfo(
    modifier: Modifier = Modifier,
    isUnlimited: Boolean,
) {
    Surface(modifier = modifier) {
        CommonItem(
            icon = painterResource(id = R.drawable.ic_outline_money_24),
            title = if (isUnlimited) stringResource(id = R.string.title_flat_fee) else stringResource(
                id = R.string.title_measured_rate
            ),
            description = stringResource(id = R.string.description_fee)
        )
    }
}