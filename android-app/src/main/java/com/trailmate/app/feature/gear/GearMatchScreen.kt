package com.trailmate.app.feature.gear

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trailmate.app.core.design.TrailMateGlyph
import com.trailmate.app.core.design.TrailMateIconBadge
import com.trailmate.app.core.design.TrailMateLineIcon
import com.trailmate.app.core.design.TrailMatePanel
import com.trailmate.app.core.design.TrailMatePanelTone
import com.trailmate.app.core.design.TrailMateSectionHeader
import com.trailmate.app.core.design.TrailMateSegmentedControl
import com.trailmate.app.core.design.TrailMateStatusPill
import com.trailmate.app.core.model.GearCatalogSelectionEngine
import com.trailmate.app.core.model.GearCatalogRouteMatchTone
import com.trailmate.app.core.model.GearCatalogThumbnailPolicy
import com.trailmate.app.core.model.GearCatalogItem
import com.trailmate.app.core.model.GearRecommendation
import com.trailmate.app.core.model.GearStatus
import com.trailmate.app.core.model.TrailMateGearCatalogPreviewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun GearMatchScreen(
    routeGearRecommendations: List<GearRecommendation>,
    requestedCategory: String,
    catalogItems: List<GearCatalogItem> = TrailMateGearCatalogPreviewData.items,
    catalogStatusLabel: String = "品牌库",
    catalogStatusCaption: String = "品牌、型号和缩略图由服务端统一维护。",
    catalogIsLoading: Boolean = false,
    onRetryCatalogLoad: (() -> Unit)? = null
) {
    var category by rememberSaveable { mutableStateOf(requestedCategory.ifBlank { "登山杖" }) }
    var catalogQuery by rememberSaveable { mutableStateOf("") }
    var selectedTabLabel by rememberSaveable { mutableStateOf(GearMatchTab.RouteNeeds.label) }
    var selectedCatalogItemId by rememberSaveable { mutableStateOf(catalogItems.firstOrNull()?.catalogItemId.orEmpty()) }
    val selectedTab = GearMatchTab.entries.first { tab -> tab.label == selectedTabLabel }
    val selectedCatalogItem = catalogItems.firstOrNull { item -> item.catalogItemId == selectedCatalogItemId }
        ?: GearCatalogSelectionEngine.matchCatalogItems(catalogItems, category, catalogQuery).firstOrNull()
        ?: catalogItems.firstOrNull()
    LaunchedEffect(requestedCategory) {
        if (requestedCategory.isNotBlank()) {
            category = requestedCategory
            selectedTabLabel = GearMatchTab.BrandMatches.label
        }
    }

    LaunchedEffect(selectedCatalogItem?.catalogItemId) {
        selectedCatalogItemId = selectedCatalogItem?.catalogItemId.orEmpty()
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "装备匹配",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            TrailMateStatusPill(
                text = catalogStatusLabel,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
        TrailMateSegmentedControl(
            labels = GearMatchTab.entries.map { it.label },
            selected = selectedTab.label,
            onSelected = { label -> selectedTabLabel = label }
        )
        if (selectedTab == GearMatchTab.RouteNeeds) {
            GearCatalogSourceStatusPanel(
                label = catalogStatusLabel,
                caption = catalogStatusCaption,
                isLoading = catalogIsLoading,
                onRetryCatalogLoad = onRetryCatalogLoad
            )
        }
        when (selectedTab) {
            GearMatchTab.RouteNeeds -> RouteGearChecklistTab(
                recommendations = routeGearRecommendations,
                catalogItems = catalogItems,
                onSelectCategory = { category = it },
                onOpenCatalog = { selectedTabLabel = GearMatchTab.BrandMatches.label }
            )

            GearMatchTab.BrandMatches -> {
                SelectCatalogGearPanel(
                    category = category,
                    catalogQuery = catalogQuery,
                    catalogItems = catalogItems,
                    catalogStatusLabel = catalogStatusLabel,
                    catalogStatusCaption = catalogStatusCaption,
                    catalogIsLoading = catalogIsLoading,
                    onCatalogQueryChange = { catalogQuery = it },
                    onRetryCatalogLoad = onRetryCatalogLoad,
                    onBackToRouteNeeds = { selectedTabLabel = GearMatchTab.RouteNeeds.label },
                    onOpenCatalogItem = { item ->
                        selectedCatalogItemId = item.catalogItemId
                        selectedTabLabel = GearMatchTab.Details.label
                    }
                )
            }

            GearMatchTab.Details -> GearDetailsTab(
                selectedCatalogItem = selectedCatalogItem
            )
        }
    }
}

internal enum class GearMatchTab(val label: String) {
    RouteNeeds("路线需求"),
    BrandMatches("品牌候选"),
    Details("装备详情")
}

@Composable
private fun GearCatalogSourceStatusPanel(
    label: String,
    caption: String,
    isLoading: Boolean,
    onRetryCatalogLoad: (() -> Unit)?
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gear-catalog-source-status"),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.66f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrailMateIconBadge(glyph = TrailMateGlyph.Gear, modifier = Modifier.size(42.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "品牌装备库",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TrailMateStatusPill(
                        text = if (isLoading) "同步中" else label,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onRetryCatalogLoad != null) {
                OutlinedButton(
                    onClick = onRetryCatalogLoad,
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("gear-catalog-retry")
                ) {
                    Text(
                        text = "重试同步",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteGearChecklistTab(
    recommendations: List<GearRecommendation>,
    catalogItems: List<GearCatalogItem>,
    onSelectCategory: (String) -> Unit,
    onOpenCatalog: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AiGearRecommendationBanner(hasRecommendations = recommendations.isNotEmpty())
        val matchedCatalogCount = recommendations.count { recommendation ->
            GearCatalogSelectionEngine.matchCatalogItems(catalogItems, recommendation.category, "").isNotEmpty()
        }
        TrailMateSectionHeader(
            title = "路线装备需求",
            action = "$matchedCatalogCount/${recommendations.size.coerceAtLeast(12)} 匹配"
        )
        if (recommendations.isEmpty()) {
            RouteGearEmptyState(onOpenCatalog = onOpenCatalog)
            return@Column
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            recommendations.forEachIndexed { index, recommendation ->
                val matchedCatalogItem = GearCatalogSelectionEngine
                    .matchCatalogItems(catalogItems, recommendation.category, "")
                    .firstOrNull()
                GearChecklistRow(
                    recommendation = recommendation,
                    matchedCatalogItem = matchedCatalogItem,
                    onViewMatches = {
                        onSelectCategory(recommendation.category)
                        onOpenCatalog()
                    }
                )
                if (index < recommendations.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.84f))
                    )
                }
            }
        }
        Text(
            text = "展开全部装备需求 12 项⌄",
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun RouteGearEmptyState(onOpenCatalog: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TrailMateIconBadge(glyph = TrailMateGlyph.Gear, modifier = Modifier.size(48.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "导入路线后生成装备需求",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "这里会根据距离、爬升和天气生成出发检查项，并从服务端品牌库匹配品牌装备。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            OutlinedButton(
                onClick = onOpenCatalog,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("查看品牌候选")
            }
        }
    }
}

@Composable
private fun AiGearRecommendationBanner(hasRecommendations: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrailMateIconBadge(glyph = TrailMateGlyph.Spark, modifier = Modifier.size(48.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "AI 装备需求",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TrailMateStatusPill(
                        text = "Beta",
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = if (hasRecommendations) "基于路线数据与天气，为你推荐必备装备清单。" else "导入路线后生成装备清单。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TrailMateStatusPill(
                text = "路线驱动",
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun GearChecklistRow(
    recommendation: GearRecommendation,
    matchedCatalogItem: GearCatalogItem?,
    onViewMatches: () -> Unit
) {
    val needsMatchedItemCheck = matchedCatalogItem != null &&
        recommendation.status == GearStatus.CHECK &&
        recommendation.category.contains("头灯")
    val matchPresentation = GearCatalogSelectionEngine.presentRouteMatch(
        recommendation = recommendation,
        matchedCatalogItem = matchedCatalogItem
    )
    val visualStatus = when {
        matchedCatalogItem != null && !needsMatchedItemCheck -> GearStatus.COVERED
        else -> recommendation.status
    }
    val statusColor = when (visualStatus) {
        GearStatus.COVERED -> MaterialTheme.colorScheme.primary
        GearStatus.CHECK -> Color(0xFFF2B21A)
        GearStatus.MISSING -> Color(0xFFE0463F)
        GearStatus.OPTIONAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val statusGlyph = when (visualStatus) {
        GearStatus.COVERED -> TrailMateGlyph.Check
        GearStatus.CHECK -> TrailMateGlyph.Warning
        GearStatus.MISSING -> TrailMateGlyph.Minus
        GearStatus.OPTIONAL -> TrailMateGlyph.Add
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(statusColor),
            contentAlignment = Alignment.Center
        ) {
            TrailMateLineIcon(
                glyph = statusGlyph,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Color.White
            )
        }
        CatalogGearThumbnail(
            item = matchedCatalogItem,
            fallbackCategory = recommendation.category
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = recommendation.category,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = matchPresentation.statusLine,
                style = MaterialTheme.typography.bodyMedium,
                color = when (matchPresentation.tone) {
                    GearCatalogRouteMatchTone.MATCHED -> MaterialTheme.colorScheme.primary
                    GearCatalogRouteMatchTone.CHECK -> Color(0xFFC97800)
                    GearCatalogRouteMatchTone.MISSING -> Color(0xFFE0463F)
                    GearCatalogRouteMatchTone.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        OutlinedButton(
            onClick = onViewMatches,
            modifier = Modifier.height(38.dp)
        ) {
            Text(
                text = "查看匹配",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
        TrailMateLineIcon(
            glyph = TrailMateGlyph.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        GearThumbnail(category = item?.category ?: fallbackCategory)
    }
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

@Composable
private fun GearThumbnail(category: String) {
    val lineColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(Color(0xFFDCE8D8), Color(0xFFEEF3EA))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(13.dp)
        ) {
            val stroke = Stroke(width = 3.4.dp.toPx(), cap = StrokeCap.Round)
            val w = size.width
            val h = size.height
            when {
                category.contains("头灯") -> {
                    drawLine(lineColor, Offset(w * 0.05f, h * 0.44f), Offset(w * 0.95f, h * 0.44f), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                    drawCircle(lineColor, radius = w * 0.2f, center = Offset(w * 0.5f, h * 0.45f), style = stroke)
                    drawLine(lineColor, Offset(w * 0.5f, h * 0.66f), Offset(w * 0.5f, h * 0.92f), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                }
                category.contains("登山杖") -> {
                    drawLine(lineColor, Offset(w * 0.28f, h * 0.06f), Offset(w * 0.1f, h * 0.92f), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(lineColor, Offset(w * 0.72f, h * 0.06f), Offset(w * 0.9f, h * 0.92f), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(lineColor, Offset(w * 0.18f, h * 0.18f), Offset(w * 0.38f, h * 0.18f), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(lineColor, Offset(w * 0.62f, h * 0.18f), Offset(w * 0.82f, h * 0.18f), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                }
                else -> {
                    drawLine(lineColor, Offset(w * 0.5f, h * 0.08f), Offset(w * 0.18f, h * 0.3f), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(lineColor, Offset(w * 0.5f, h * 0.08f), Offset(w * 0.82f, h * 0.3f), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(lineColor, Offset(w * 0.18f, h * 0.3f), Offset(w * 0.18f, h * 0.9f), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(lineColor, Offset(w * 0.82f, h * 0.3f), Offset(w * 0.82f, h * 0.9f), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(lineColor, Offset(w * 0.18f, h * 0.9f), Offset(w * 0.82f, h * 0.9f), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(lineColor, Offset(w * 0.5f, h * 0.2f), Offset(w * 0.5f, h * 0.9f), strokeWidth = 2.4.dp.toPx(), cap = StrokeCap.Round)
                }
            }
        }
    }
}

@Composable
private fun CatalogGearCandidateRow(
    item: GearCatalogItem,
    onOpenDetails: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CatalogGearThumbnail(
                item = item,
                fallbackCategory = item.category
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = listOfNotNull(
                        item.category,
                        item.weightGrams?.let { "${it}g" }
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val tags = item.tags.take(3).joinToString(" / ")
                Text(
                    text = if (item.imageUrl.isNullOrBlank()) tags else "$tags · 图片已配置",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Button(
                onClick = onOpenDetails,
                modifier = Modifier
                    .height(38.dp)
                    .testTag("gear-catalog-candidate-details")
            ) {
                Text(
                    text = "详情",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SelectCatalogGearPanel(
    category: String,
    catalogQuery: String,
    catalogItems: List<GearCatalogItem>,
    catalogStatusLabel: String,
    catalogStatusCaption: String,
    catalogIsLoading: Boolean,
    onCatalogQueryChange: (String) -> Unit,
    onRetryCatalogLoad: (() -> Unit)?,
    onBackToRouteNeeds: () -> Unit,
    onOpenCatalogItem: (GearCatalogItem) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val visibleCatalogItems = catalogItems
        .filter { item -> item.matchesCatalogFilter(category = category, query = catalogQuery) }
        .take(6)
    val categoryLabel = category.ifBlank { "全部类别" }
    val shouldRequestInputFocusOnOpen = if (catalogQuery.isBlank()) {
        GearCatalogSearchUiState.initial(category).requestInputFocus
    } else {
        GearCatalogSearchUiState.initial(category).withQuery(catalogQuery).requestInputFocus
    }

    LaunchedEffect(Unit) {
        if (!shouldRequestInputFocusOnOpen) {
            focusManager.clearFocus(force = true)
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "品牌候选装备",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.68f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "当前需求：$categoryLabel",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "由路线需求决定",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = catalogQuery,
                    onValueChange = onCatalogQueryChange,
                    label = { Text("搜索品牌 / 型号") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = if (catalogIsLoading) "同步中" else catalogStatusLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = catalogStatusCaption,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                if (onRetryCatalogLoad != null) {
                    OutlinedButton(
                        onClick = onRetryCatalogLoad,
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("gear-catalog-retry")
                    ) {
                        Text(
                            text = "重试",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (visibleCatalogItems.isEmpty()) {
                CatalogEmptyMatchState(
                    category = categoryLabel,
                    onBackToRouteNeeds = onBackToRouteNeeds
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    visibleCatalogItems.forEach { item ->
                        CatalogGearCandidateRow(
                            item = item,
                            onOpenDetails = { onOpenCatalogItem(item) }
                        )
                    }
                }
            }
            Text(
                text = "未收录型号由 TrailMate 后台补充；手机端只查看本次路线匹配结果。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CatalogEmptyMatchState(
    category: String,
    onBackToRouteNeeds: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gear-catalog-empty-match"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrailMateIconBadge(glyph = TrailMateGlyph.Spark, modifier = Modifier.size(42.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "服务端暂未收录$category",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "品牌、型号和缩略图由 TrailMate 后台统一维护；手机端只查看本次路线匹配结果。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        OutlinedButton(
            onClick = onBackToRouteNeeds,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("返回路线需求")
        }
    }
}

private fun GearCatalogItem.matchesCatalogFilter(category: String, query: String): Boolean {
    return GearCatalogSelectionEngine.matchCatalogItems(
        catalogItems = listOf(this),
        routeCategory = category,
        query = query
    ).isNotEmpty()
}

@Composable
private fun GearDetailsTab(
    selectedCatalogItem: GearCatalogItem?
) {
    if (selectedCatalogItem == null) {
        TrailMatePanel(
            title = "装备详情",
            value = "等待匹配",
            caption = "先从候选装备查看当前路线类别的服务端匹配。",
            tone = TrailMatePanelTone.Neutral
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TrailMatePanel(
            title = selectedCatalogItem.category,
            value = "${selectedCatalogItem.brand} ${selectedCatalogItem.model}",
            caption = listOfNotNull(
                selectedCatalogItem.weightGrams?.let { "${it}g" },
                selectedCatalogItem.tags.take(3).joinToString(" / ").ifBlank { null }
            ).joinToString(" · "),
            tone = TrailMatePanelTone.Primary
        )
        TrailMatePanel(
            title = "缩略图",
            value = if (selectedCatalogItem.imageUrl.isNullOrBlank()) "使用类别缩略图" else "服务端图片已配置",
            caption = selectedCatalogItem.imageAttribution ?: "图片由服务端品牌库统一维护。",
            tone = TrailMatePanelTone.Neutral
        )
    }
}
