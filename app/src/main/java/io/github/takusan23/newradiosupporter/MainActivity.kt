package io.github.takusan23.newradiosupporter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import io.github.takusan23.newradiosupporter.tool.CrashReportGenerator
import io.github.takusan23.newradiosupporter.ui.screen.NewRadioSupporterMainScreen
import org.lsposed.hiddenapibypass.HiddenApiBypass

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        HiddenApiBypass.addHiddenApiExemptions("")
        CrashReportGenerator.initCrashReportGenerator(this)

        setContent { NewRadioSupporterMainScreen() }
    }

}