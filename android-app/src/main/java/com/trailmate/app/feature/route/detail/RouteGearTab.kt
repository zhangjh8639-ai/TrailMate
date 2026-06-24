package com.trailmate.app.feature.route.detail

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trailmate.app.core.design.TrailMateGlyph
import com.trailmate.app.core.design.TrailMateLineIcon
import com.trailmate.app.core.design.TrailMateStatusPill
import com.trailmate.app.core.model.AiGearAdvisorPresentation
import com.trailmate.app.core.model.GearCatalogItem
import com.trailmate.app.core.model.GearCatalogRouteMatchTone
import com.trailmate.app.core.model.GearCatalogSelectionEngine
import com.trailmate.app.core.model.GearCatalogThumbnailPolicy
import com.trailmate.app.core.model.GearRecommendation
import com.trailmate.app.core.model.GearStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
internal fun RouteGearTab(
    recommendations: List<GearRecommendation>,
    catalogItems: List<GearCatalogItem>,
    catalogStatusLabel: String,
    aiGearAdvisorPresentation: AiGearAdvisorPresentation,
    onViewGearMatches: (String) -> Unit
) {
    val routeChecklist = recommendations.sortedWith(
        compareBy<GearRecommendation> { it.status.routeGearSortOrder() }
            .thenBy { it.category }
    )
    val requiredItems = routeChecklist.filterNot { it.status == GearStatus.OPTIONAL }
    val matchedCatalogCount = requiredItems.count { recommendation ->
        GearCatalogSelectionEngine.matchCatalogItems(catalogItems, recommendation.category, "").isNotEmpty()
    }
    val unmatchedCount = requiredItems.size - matchedCatalogCount

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GearAdvisorCard(
            presentation = aiGearAdvisorPresentation,
            catalogStatusLabel = catalogStatusLabel
        )
        GearChecklistHeader(
            matchedCount = matchedCatalogCount,
            requiredCount = requiredItems.size,
            unmatchedCount = unmatchedCount
        )
        routeChecklist.forEach { item ->
            val matchedCatalogItem = GearCatalogSelectionEngine
                .matchCatalogItems(catalogItems, item.category, "")
                .firstOrNull()
            GearChecklistRow(
                recommendation = item,
                matchedCatalogItem = matchedCatalogItem,
                onViewGearMatches = onViewGearMatches
            )
        }
    }
}

@Composable
private fun GearAdvisorCard(
    presentation: AiGearAdvisorPresentation,
    catalogStatusLabel: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.82f)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = TrailMateGlyph.Spark,
                    contentDescription = null,
                    modifier = Modifier.size(25.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI 装备需求",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    TrailMateStatusPill(
                        text = "Beta",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = presentation.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TrailMateStatusPill(
                text = presentation.statusLabel,
                containerColor = presentation.statusContainerColor(),
                contentColor = presentation.statusContentColor()
            )
        }
    }
    Text(
        text = "$catalogStatusLabel：手机端只展示本次路线匹配结果，品牌、型号和图片由服务端品牌库维护。",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun GearChecklistHeader(
    matchedCount: Int,
    requiredCount: Int,
    unmatchedCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "路线装备需求",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "服务端匹配 $matchedCount/$requiredCount · 待匹配 $unmatchedCount 项",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TrailMateStatusPill(
            text = if (unmatchedCount == 0) "候选齐全" else "待匹配",
            containerColor = if (unmatchedCount == 0) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            } else {
                Color(0xFFFFE6E2)
            },
            contentColor = if (unmatchedCount == 0) {
                MaterialTheme.colorScheme.primary
            } else {
                Color(0xFFB3261E)
            }
        )
    }
}

@Composable
private fun GearChecklistRow(
    recommendation: GearRecommendation,
    matchedCatalogItem: GearCatalogItem?,
    onViewGearMatches: (String) -> Unit
) {
    val needsMatchedItemCheck = matchedCatalogItem != null &&
        recommendation.status == GearStatus.CHECK &&
        recommendation.category.contains("头灯")
    val visualStatus = when {
        matchedCatalogItem != null && !needsMatchedItemCheck -> GearStatus.COVERED
        else -> recommendation.status
    }
    val statusColor = visualStatus.statusColor()
    val matchPresentation = GearCatalogSelectionEngine.presentRouteMatch(
        recommendation = recommendation,
        matchedCatalogItem = matchedCatalogItem
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.78f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = visualStatus.statusGlyph(),
                    contentDescription = null,
                    modifier = Modifier.size(21.dp),
                    tint = statusColor
                )
            }
            CatalogGearThumbnail(
                item = matchedCatalogItem,
                fallbackCategory = recommendation.category
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = recommendation.category,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = matchPresentation.statusLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = matchPresentation.tone.routeMatchColor()
                )
            }
            OutlinedButton(onClick = { onViewGearMatches(recommendation.category) }) {
                Text("查看候选")
            }
        }
    }
}

@Composable
private fun CatalogGearThumbnail(
    item: GearCatalogItem?,
    fallbackCategory: String
) {
    val imageUrl = item?.takeIf { GearCatalogThumbnailPolicy.shouldLoadServerThumbnail(it) }?.imageUrl
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = imageUrl) {
        value = imageUrl?.let { loadGearThumbnailBitmap(it) }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = item?.displayName,
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        return
    }

    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
        contentAlignment = Alignment.Center
    ) {
        TrailMateLineIcon(
            glyph = fallbackCategory.thumbnailGlyph(),
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

private fun GearStatus.statusGlyph(): TrailMateGlyph =
    when (this) {
        GearStatus.COVERED -> TrailMateGlyph.Check
        GearStatus.CHECK -> TrailMateGlyph.Warning
        GearStatus.MISSING -> TrailMateGlyph.Minus
        GearStatus.OPTIONAL -> TrailMateGlyph.Gear
    }

private fun GearStatus.statusColor(): Color =
    when (this) {
        GearStatus.COVERED -> Color(0xFF0C5D3F)
        GearStatus.CHECK -> Color(0xFFD18400)
        GearStatus.MISSING -> Color(0xFFD64945)
        GearStatus.OPTIONAL -> Color(0xFF5F6863)
    }

private fun GearStatus.routeGearSortOrder(): Int =
    when (this) {
        GearStatus.MISSING -> 0
        GearStatus.CHECK -> 1
        GearStatus.COVERED -> 2
        GearStatus.OPTIONAL -> 3
    }

@Composable
private fun AiGearAdvisorPresentation.statusContainerColor(): Color =
    when {
        isStaleResponse -> Color(0xFFFFE6E2)
        isFallbackActive -> Color(0xFFFFF3D7)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    }

@Composable
private fun AiGearAdvisorPresentation.statusContentColor(): Color =
    when {
        isStaleResponse -> Color(0xFFB3261E)
        isFallbackActive -> Color(0xFF8B5A00)
        else -> MaterialTheme.colorScheme.primary
    }

@Composable
private fun GearCatalogRouteMatchTone.routeMatchColor(): Color =
    when (this) {
        GearCatalogRouteMatchTone.MATCHED -> MaterialTheme.colorScheme.primary
        GearCatalogRouteMatchTone.CHECK -> Color(0xFFC97800)
        GearCatalogRouteMatchTone.MISSING -> Color(0xFFE0463F)
        GearCatalogRouteMatchTone.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }

private suspend fun loadGearThumbnailBitmap(imageUrl: String): Bitmap? =
    withContext(Dispatchers.IO) {
        runCatching {
            val connection = URL(imageUrl).openConnection()
            connection.connectTimeout = 1_200
            connection.readTimeout = 1_200
            connection.getInputStream().use { input ->
                BitmapFactory.decodeStream(input)
            }
        }.getOrNull()
    }

private fun String.thumbnailGlyph(): TrailMateGlyph =
    when {
        contains("头灯") -> TrailMateGlyph.Compass
        contains("登山杖") -> TrailMateGlyph.Route
        contains("备用水") -> TrailMateGlyph.Weather
        contains("雨衣") || contains("防水") -> TrailMateGlyph.Weather
        contains("保暖") || contains("保温") -> TrailMateGlyph.Gear
        else -> TrailMateGlyph.Gear
    }
