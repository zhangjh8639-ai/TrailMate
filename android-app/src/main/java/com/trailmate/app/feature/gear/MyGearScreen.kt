package com.trailmate.app.feature.gear

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.trailmate.app.core.design.TrailMateMetricRow
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
    var category by rememberSaveable { mutableStateOf(requestedCategory) }
    var brand by rememberSaveable { mutableStateOf("") }
    var model by rememberSaveable { mutableStateOf("") }
    var weightGrams by rememberSaveable { mutableStateOf("") }
    val missingCount = routeGearRecommendations.count { it.status == GearStatus.MISSING }
    val availableCount = inventory.items.count { it.available }

    LaunchedEffect(requestedCategory) {
        category = requestedCategory
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "My Gear",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        TrailMateMetricRow(
            items = listOf(
                "Owned" to inventory.items.size.toString(),
                "Available" to availableCount.toString(),
                "Route gaps" to missingCount.toString()
            )
        )
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
                onSetAvailability = { available -> onSetAvailability(item.id, available) },
                onDelete = { onDeleteGear(item.id) }
            )
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
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Add brand gear",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = category,
                    onValueChange = onCategoryChange,
                    label = { Text("Category") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = weightGrams,
                    onValueChange = onWeightChange,
                    label = { Text("Grams") },
                    modifier = Modifier.weight(0.72f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            OutlinedTextField(
                value = brand,
                onValueChange = onBrandChange,
                label = { Text("Brand") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = model,
                onValueChange = onModelChange,
                label = { Text("Model") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = onSubmit,
                enabled = category.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add to My Gear")
            }
            Text(
                text = "Route gear suggestions can be matched to your owned items. AI suggestions stay separate from deterministic route risk.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f)
            )
        }
    }
}

@Composable
private fun GearItemPanel(
    item: GearItem,
    onSetAvailability: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val name = listOfNotNull(item.brand, item.model)
        .joinToString(" ")
        .ifBlank { item.category }
    val weight = item.weightGrams?.let { "${it}g" } ?: "weight TBD"
    val availability = if (item.available) "ready" else "not packed"
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = item.available,
                        onCheckedChange = onSetAvailability
                    )
                }
                OutlinedButton(onClick = onDelete) {
                    Text("Remove")
                }
            }
        }
    }
}
