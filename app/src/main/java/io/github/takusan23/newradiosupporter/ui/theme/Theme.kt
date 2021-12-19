package io.github.takusan23.newradiosupporter.ui.theme

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple200,
    primaryContainer = Purple700,
    secondary = Teal200
)

private val LightColorScheme = lightColorScheme(
    primary = Purple500,
    primaryContainer = Purple700,
    secondary = Teal200

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@SuppressLint("NewApi")
@Composable
fun NewRadioSupporterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isUseDynamicColor: Boolean = true,
    content: @Composable() () -> Unit,
) {
    val isAndroidSnowConeAndLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val context = LocalContext.current

    val colors = when {
        darkTheme && isUseDynamicColor && isAndroidSnowConeAndLater -> dynamicDarkColorScheme(context)
        !darkTheme && isUseDynamicColor && isAndroidSnowConeAndLater -> dynamicLightColorScheme(context)
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}