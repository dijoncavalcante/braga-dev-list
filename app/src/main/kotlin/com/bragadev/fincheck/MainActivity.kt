package com.bragadev.fincheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.bragadev.fincheck.navigation.AppNavigation
import com.bragadev.fincheck.ui.theme.FinCheckTheme

/**
 * Hosts the theme and the navigation graph. Each screen owns its own Scaffold
 * (top bar, FAB, content padding), so this Activity stays a thin shell.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Before super.onCreate: shows the FinCheck launch screen (Theme.FinCheck.Starting) on
        // every Android version and then switches to Theme.FinCheck.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinCheckTheme {
                AppNavigation()
            }
        }
    }
}
