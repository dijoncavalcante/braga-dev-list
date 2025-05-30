package com.bragadev.list.features.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.bragadev.list.navigation.Screen

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Home Screen")
        Button(onClick = {
            navController.navigate(Screen.CreateItem.route)
        }) {
            Text("Go to Profile")
        }
        Button(onClick = {
            navController.popBackStack() // Simplesmente volta para a tela anterior (Login)
        }) {
            Text("Back(Pop)")
        }
    }
}
