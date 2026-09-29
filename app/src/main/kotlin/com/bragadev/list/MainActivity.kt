package com.bragadev.list

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bragadev.list.navigation.AppNavigation
import com.bragadev.list.ui.theme.BragadevlistTheme

/**
 * Hosts the theme and the navigation graph. Each screen owns its own Scaffold
 * (top bar, FAB, content padding), so this Activity stays a thin shell.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BragadevlistTheme {
                AppNavigation()
            }
        }
    }
}
