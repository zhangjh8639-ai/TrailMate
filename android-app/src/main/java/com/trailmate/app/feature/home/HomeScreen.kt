package com.trailmate.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trailmate.app.core.design.TrailMateMetricRow
import com.trailmate.app.core.design.TrailMatePanel
import com.trailmate.app.core.design.TrailMatePanelTone
import com.trailmate.app.feature.route.RouteDetailScreen

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "Trail coach",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        TrailMatePanel(
            title = "Temporary profile",
            value = "LOW",
            caption = "Import 3 GPX activities to calibrate with real history.",
            tone = TrailMatePanelTone.Secondary
        )
        TrailMateMetricRow(
            items = listOf(
                "Distance" to "15.2 km",
                "Ascent" to "+860 m",
                "ETA" to "6:40"
            )
        )
        RouteDetailScreen()
    }
}
