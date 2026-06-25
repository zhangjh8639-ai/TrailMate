package com.trailmate.app.core.design

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Backpack
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class TrailMateGlyph {
    Add,
    Back,
    Bell,
    Chart,
    Check,
    ChevronRight,
    Compass,
    Folder,
    Gear,
    Home,
    Location,
    Map,
    Minus,
    More,
    Mountain,
    Profile,
    Route,
    Spark,
    Warning,
    Weather
}

@Composable
fun TrailMateLineIcon(
    glyph: TrailMateGlyph,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Icon(
        imageVector = glyph.imageVector(),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}

private fun TrailMateGlyph.imageVector(): ImageVector =
    when (this) {
        TrailMateGlyph.Add -> Icons.Outlined.Add
        TrailMateGlyph.Back -> Icons.AutoMirrored.Outlined.KeyboardArrowLeft
        TrailMateGlyph.Bell -> Icons.Outlined.Notifications
        TrailMateGlyph.Chart -> Icons.Outlined.BarChart
        TrailMateGlyph.Check -> Icons.Outlined.Check
        TrailMateGlyph.ChevronRight -> Icons.Outlined.ChevronRight
        TrailMateGlyph.Compass -> Icons.Outlined.Explore
        TrailMateGlyph.Folder -> Icons.Outlined.FolderOpen
        TrailMateGlyph.Gear -> Icons.Outlined.Backpack
        TrailMateGlyph.Home -> Icons.Outlined.Home
        TrailMateGlyph.Location -> Icons.Outlined.MyLocation
        TrailMateGlyph.Map -> Icons.Outlined.Map
        TrailMateGlyph.Minus -> Icons.Outlined.Remove
        TrailMateGlyph.More -> Icons.Outlined.MoreHoriz
        TrailMateGlyph.Mountain -> Icons.Outlined.Terrain
        TrailMateGlyph.Profile -> Icons.Outlined.Person
        TrailMateGlyph.Route -> Icons.Outlined.Route
        TrailMateGlyph.Spark -> Icons.Outlined.AutoAwesome
        TrailMateGlyph.Warning -> Icons.Outlined.WarningAmber
        TrailMateGlyph.Weather -> Icons.Outlined.Cloud
    }
