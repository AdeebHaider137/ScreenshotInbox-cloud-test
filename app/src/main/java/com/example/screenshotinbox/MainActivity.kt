package com.example.screenshotinbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.screenshotinbox.data.local.ThemeMode
import com.example.screenshotinbox.navigation.ScreenshotInboxNavGraph
import com.example.screenshotinbox.ui.theme.ScreenshotInboxTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ScreenshotInboxApp

        setContent {
            val themeMode by app.preferencesManager.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

            ScreenshotInboxTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ScreenshotInboxNavGraph(
                        screenshotRepository = app.screenshotRepository,
                        categoryRepository = app.categoryRepository,
                        preferencesManager = app.preferencesManager
                    )
                }
            }
        }
    }
}
