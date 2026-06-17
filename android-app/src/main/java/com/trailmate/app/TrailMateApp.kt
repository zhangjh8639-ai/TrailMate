package com.trailmate.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.trailmate.app.feature.home.HomeScreen
import com.trailmate.app.feature.onboarding.OnboardingScreen

enum class TrailMateScreen {
    ONBOARDING,
    HOME
}

@Composable
fun TrailMateApp() {
    var screen by rememberSaveable { mutableStateOf(TrailMateScreen.ONBOARDING) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (screen) {
            TrailMateScreen.ONBOARDING -> OnboardingScreen(
                onComplete = { screen = TrailMateScreen.HOME }
            )
            TrailMateScreen.HOME -> HomeScreen()
        }
    }
}
