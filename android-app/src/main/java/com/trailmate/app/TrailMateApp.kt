package com.trailmate.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.trailmate.app.feature.home.HomeScreen
import com.trailmate.app.feature.onboarding.OnboardingScreen

enum class TrailMateScreen {
    ONBOARDING,
    HOME
}

@Composable
fun TrailMateApp() {
    var screen by remember { mutableStateOf(TrailMateScreen.ONBOARDING) }

    when (screen) {
        TrailMateScreen.ONBOARDING -> OnboardingScreen(
            onComplete = { screen = TrailMateScreen.HOME }
        )
        TrailMateScreen.HOME -> HomeScreen()
    }
}
