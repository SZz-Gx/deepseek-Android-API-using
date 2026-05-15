package com.example.deepseekchat.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.deepseekchat.ui.chat.ChatScreen
import com.example.deepseekchat.ui.chat.ChatViewModel
import com.example.deepseekchat.ui.settings.SettingsScreen
import java.math.BigDecimal

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val viewModel: ChatViewModel = viewModel()

    NavHost(navController = navController, startDestination = "chat") {
        composable("chat") {
            ChatScreen(viewModel, onNavigateToSettings = { navController.navigate("settings") })
        }
        composable("settings") {
            SettingsScreen(
                onModelChanged = { viewModel.model = it }, onSystemPromptChanged = { viewModel.systemPrompt = it },
                onMaxContextRoundsChanged = { viewModel.maxContextRounds = it },
                onInputPriceMissChanged = { viewModel.inputPriceMiss = it }, onInputPriceHitChanged = { viewModel.inputPriceHit = it },
                onOutputPriceChanged = { viewModel.outputPrice = it }, onNavigateBack = { navController.popBackStack() },
                onNavigateToApiConfigs = { navController.navigate("api_configs") }
            )
        }
        composable("api_configs") {
            SettingsScreen(
                onModelChanged = { viewModel.model = it }, onSystemPromptChanged = { viewModel.systemPrompt = it },
                onMaxContextRoundsChanged = { viewModel.maxContextRounds = it },
                onInputPriceMissChanged = { viewModel.inputPriceMiss = it }, onInputPriceHitChanged = { viewModel.inputPriceHit = it },
                onOutputPriceChanged = { viewModel.outputPrice = it }, onNavigateBack = { navController.popBackStack() },
                showApiConfigs = true
            )
        }
    }
}
