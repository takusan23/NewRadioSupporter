package io.github.takusan23.newradiosupporter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.takusan23.newradiosupporter.ui.screen.NewRadioSupporterMainScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NewRadioSupporterMainScreen() }
    }

}