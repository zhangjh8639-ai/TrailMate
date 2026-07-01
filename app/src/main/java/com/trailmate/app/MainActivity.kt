package com.trailmate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.trailmate.app.ui.TrailMateApp
import com.trailmate.app.ui.theme.TrailMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrailMateTheme {
                TrailMateApp()
            }
        }
    }
}
