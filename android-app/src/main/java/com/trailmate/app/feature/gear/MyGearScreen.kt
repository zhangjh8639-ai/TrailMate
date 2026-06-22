package com.trailmate.app.feature.gear

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.trailmate.app.core.design.TrailMateGlyph
import com.trailmate.app.core.design.TrailMateIconBadge
import com.trailmate.app.core.design.TrailMateLineIcon
import com.trailmate.app.core.design.TrailMatePanel
import com.trailmate.app.core.design.TrailMatePanelTone
import com.trailmate.app.core.design.TrailMateSectionHeader
import com.trailmate.app.core.design.TrailMateSegmentedControl
import com.trailmate.app.core.design.TrailMateStatusPill
import com.trailmate.app.core.model.GearDetailEngine
import com.trailmate.app.core.model.GearDetailSummary
import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.GearItem
import com.trailmate.app.core.model.GearRecommendation
import com.trailmate.app.core.model.GearStatus

@Composable
fun MyGearScreen(
    inventory: GearInventory,
    routeGearRecommendations: List<GearRecommendation>,
    requestedCategory: String,
    onAddBrandGear: (category: String, brand: String?, model: String?, weightGrams: Int?) -> Unit,
    onSetAvailability: (itemId: String, available: Boolean) -> Unit,
    onDeleteGear: (itemId: String) -> Unit
) {
    var category by rememberSaveable { mutableStateOf(requestedCategory.ifBlank { "登山杖" }) }
    var brand by rememberSaveable { mutableStateOf("") }
    var model by rememberSaveable { mutableStateOf("") }
    var weightGrams by rememberSaveable { mutableStateOf("") }
    var selectedTabLabel by rememberSaveable { mutableStateOf(MyGearTab.RouteList.label) }
    var selectedGearId by rememberSaveable { mutableStateOf(inventory.items.firstOrNull()?.id.orEmpty()) }
    val selectedTab = MyGearTab.entries.first { tab -> tab.label == selectedTabLabel }
    val hasRouteGearRecommendations = routeGearRecommendations.isNotEmpty()
    val selectedGear = inventory.items.firstOrNull { item -> item.id == selectedGearId }
        ?: inventory.items.firstOrNull()
    LaunchedEffect(requestedCategory) {
        if (requestedCategory.isNotBlank()) {
            category = requestedCategory
            selectedTabLabel = MyGearTab.Inventory.label
        }
    }

    LaunchedEffect(selectedGear?.id) {
        selectedGearId = selectedGear?.id.orEmpty()
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "装备",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            TrailMateStatusPill(
                text = if (hasRouteGearRecommendations) "龙井山脊 · 谨慎尝试" else "等待路线",
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
        TrailMateSegmentedControl(
            labels = MyGearTab.entries.map { it.label },
            selected = selectedTab.label,
            onSelected = { label -> selectedTabLabel = label }
        )
        when (selectedTab) {
            MyGearTab.RouteList -> RouteGearChecklistTab(
                recommendations = routeGearRecommendations,
                inventory = inventory,
                category = category,
                brand = brand,
                model = model,
                weightGrams = weightGrams,
                onCategoryChange = { category = it },
                onBrandChange = { brand = it },
                onModelChange = { model = it },
                onWeightChange = { weightGrams = it.filter(Char::isDigit) },
                onSelectCategory = { category = it },
                onOpenInventory = { selectedTabLabel = MyGearTab.Inventory.label },
                onSubmit = {
                    onAddBrandGear(
                        category.trim(),
                        brand.trim(),
                        model.trim(),
                        weightGrams.toIntOrNull()
                    )
                    brand = ""
                    model = ""
                    weightGrams = ""
                }
            )

            MyGearTab.Inventory -> {
                AddBrandGearPanel(
                    category = category,
                    brand = brand,
                    model = model,
                    weightGrams = weightGrams,
                    onCategoryChange = { category = it },
                    onBrandChange = { brand = it },
                    onModelChange = { model = it },
                    onWeightChange = { weightGrams = it.filter(Char::isDigit) },
                    onSubmit = {
                        onAddBrandGear(
                            category.trim(),
                            brand.trim(),
                            model.trim(),
                            weightGrams.toIntOrNull()
                        )
                        brand = ""
                        model = ""
                        weightGrams = ""
                    }
                )
                inventory.items.forEach { item ->
                    GearItemPanel(
                        item = item,
                        onViewDetails = {
                            selectedGearId = item.id
                            selectedTabLabel = MyGearTab.Details.label
                        },
                        onSetAvailability = { available -> onSetAvailability(item.id, available) },
                        onDelete = { onDeleteGear(item.id) }
                    )
                }
            }

            MyGearTab.Details -> GearDetailsTab(
                selectedGear = selectedGear,
                routeGearRecommendations = routeGearRecommendations
            )
        }
    }
}

private enum class MyGearTab(val label: String) {
    RouteList("路线清单"),
    Inventory("我的装备"),
    Details("详情")
}

@Composable
private fun RouteGearChecklistTab(
    recommendations: List<GearRecommendation>,
    inventory: GearInventory,
    category: String,
    brand: String,
    model: String,
    weightGrams: String,
    onCategoryChange: (String) -> Unit,
    onBrandChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onOpenInventory: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AiGearRecommendationBanner(hasRecommendations = recommendations.isNotEmpty())
        TrailMateSectionHeader(
            title = "必备装备",
            action = "${recommendations.count { it.status != GearStatus.MISSING }}/${recommendations.size.coerceAtLeast(12)}　按重要性⌄"
        )
        if (recommendations.isEmpty()) {
            RouteGearEmptyState(onOpenInventory = onOpenInventory)
            return@Column
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            recommendations.forEachIndexed { index, recommendation ->
                val matchedItem = recommendation.matchedGearItemId?.let { id ->
                    inventory.items.firstOrNull { item -> item.id == id }
                }
                GearChecklistRow(
                    recommendation = recommendation,
                    matchedItem = matchedItem,
                    onSelectCategory = { onSelectCategory(recommendation.category) }
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
            text = "展开全部 12 项⌄",
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        AddBrandGearPanel(
            category = category,
            brand = brand,
            model = model,
            weightGrams = weightGrams,
            onCategoryChange = onCategoryChange,
            onBrandChange = onBrandChange,
            onModelChange = onModelChange,
            onWeightChange = onWeightChange,
            onSubmit = onSubmit
        )
    }
}

@Composable
private fun RouteGearEmptyState(onOpenInventory: () -> Unit) {
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
                        text = "导入路线后生成清单",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "这里会根据距离、爬升、天气和已有装备生成出发检查项。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            OutlinedButton(
                onClick = onOpenInventory,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("先维护我的装备")
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
                        text = "AI 装备建议",
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
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "可重试",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "使用本地清单",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    TrailMateLineIcon(
                        glyph = TrailMateGlyph.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun GearChecklistRow(
    recommendation: GearRecommendation,
    matchedItem: GearItem?,
    onSelectCategory: () -> Unit
) {
    val needsMatchedItemCheck = matchedItem != null &&
        recommendation.status == GearStatus.CHECK &&
        recommendation.category.contains("头灯")
    val visualStatus = when {
        matchedItem != null && !needsMatchedItemCheck -> GearStatus.COVERED
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
        GearThumbnail(category = recommendation.category)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = recommendation.category,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            val stateLine = when {
                needsMatchedItemCheck -> "电量建议 ≥ 80%"
                matchedItem != null -> "已匹配 ${matchedItem.displayName()}"
                recommendation.status == GearStatus.CHECK -> "电量建议 ≥ 80%"
                recommendation.status == GearStatus.MISSING -> "未添加"
                else -> recommendation.rationale
            }
            Text(
                text = stateLine,
                style = MaterialTheme.typography.bodyMedium,
                color = if (recommendation.status == GearStatus.MISSING) {
                    Color(0xFFE0463F)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        if (recommendation.status == GearStatus.MISSING) {
            Text(
                text = "添加已有装备",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onSelectCategory)
            )
        } else {
            OutlinedButton(
                onClick = onSelectCategory,
                modifier = Modifier.height(38.dp)
            ) {
                Text(
                    text = "查看匹配",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
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
private fun AddBrandGearPanel(
    category: String,
    brand: String,
    model: String,
    weightGrams: String,
    onCategoryChange: (String) -> Unit,
    onBrandChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.dp,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 54.dp, height = 5.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            Text(
                text = "添加已有装备",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "快速从我的装备中选择",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = category,
                    onValueChange = onCategoryChange,
                    label = { Text("类别") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = weightGrams,
                    onValueChange = onWeightChange,
                    label = { Text("克重") },
                    modifier = Modifier.weight(0.72f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            OutlinedTextField(
                value = brand,
                onValueChange = onBrandChange,
                label = { Text("品牌") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = model,
                onValueChange = onModelChange,
                label = { Text("型号") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = onSubmit,
                enabled = category.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存到我的装备")
            }
        }
    }
}

@Composable
private fun GearItemPanel(
    item: GearItem,
    onViewDetails: () -> Unit,
    onSetAvailability: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val name = listOfNotNull(item.brand, item.model)
        .joinToString(" ")
        .ifBlank { item.category }
    val weight = item.weightGrams?.let { "${it}g" } ?: "重量待填"
    val availability = if (item.available) "可用" else "未打包"
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = if (item.available) {
            colorScheme.primary.copy(alpha = 0.11f)
        } else {
            colorScheme.surfaceVariant.copy(alpha = 0.74f)
        }
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurface.copy(alpha = 0.65f),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$weight / $availability",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurface.copy(alpha = 0.68f)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "可用",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = item.available,
                        onCheckedChange = onSetAvailability
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onViewDetails,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("查看详情")
                    }
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("移除")
                    }
                }
            }
        }
    }
}

@Composable
private fun GearDetailsTab(
    selectedGear: GearItem?,
    routeGearRecommendations: List<GearRecommendation>
) {
    if (selectedGear == null) {
        TrailMatePanel(
            title = "装备详情",
            value = "还没有装备",
            caption = "先添加品牌装备，再查看它与路线建议的匹配关系。",
            tone = TrailMatePanelTone.Neutral
        )
        return
    }

    val summary = GearDetailEngine.summarize(
        item = selectedGear,
        routeGearRecommendations = routeGearRecommendations
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GearDetailSummaryPanel(summary)
        TrailMatePanel(
            title = "路线适配",
            value = summary.routeMatchLine,
            caption = summary.routeRationale,
            tone = TrailMatePanelTone.Primary
        )
    }
}

@Composable
private fun GearDetailSummaryPanel(summary: GearDetailSummary) {
    TrailMatePanel(
        title = "装备详情",
        value = summary.title,
        caption = "${summary.category} / ${summary.statusLine}",
        tone = TrailMatePanelTone.Neutral
    )
}

private fun GearItem?.matchedGearCaption(): String =
    this?.let { item ->
        val name = item.displayName()
        " 已匹配 $name。"
    }.orEmpty()

private fun GearItem.displayName(): String =
    listOfNotNull(brand, model).joinToString(" ").ifBlank { category }

private fun GearStatus.displayLabel(): String =
    when (this) {
        GearStatus.COVERED -> "已覆盖"
        GearStatus.CHECK -> "需检查"
        GearStatus.MISSING -> "缺失"
        GearStatus.OPTIONAL -> "可选"
    }
