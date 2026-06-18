package com.trailmate.app.core.persistence

import com.trailmate.app.core.model.AscentExperience
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.ExerciseFrequency
import com.trailmate.app.core.model.ExperienceLevel
import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.GearItem
import com.trailmate.app.core.model.HistoricalActivity
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.RouteImportStatus
import com.trailmate.app.core.model.TypicalDuration
import java.io.StringReader
import java.io.StringWriter
import java.util.Properties

object TrailMateSnapshotCodec {
    fun encode(snapshot: TrailMateSnapshot): String {
        val properties = Properties()

        properties["version"] = "1"
        snapshot.profile?.let { profile ->
            properties["profile.present"] = "true"
            properties["profile.exerciseFrequency"] = profile.exerciseFrequency.name
            properties["profile.typicalDuration"] = profile.typicalDuration.name
            properties["profile.experienceLevel"] = profile.experienceLevel.name
            properties["profile.ascentExperience"] = profile.ascentExperience.name
            properties["profile.heightCm"] = profile.heightCm?.toString().orEmpty()
            properties["profile.weightKg"] = profile.weightKg?.toString().orEmpty()
            properties["profile.commonPackWeightKg"] = profile.commonPackWeightKg?.toString().orEmpty()
        }

        properties["inventory.count"] = snapshot.inventory.items.size.toString()
        snapshot.inventory.items.forEachIndexed { index, item ->
            val prefix = "inventory.$index"
            properties["$prefix.id"] = item.id
            properties["$prefix.category"] = item.category
            properties["$prefix.brand"] = item.brand.orEmpty()
            properties["$prefix.model"] = item.model.orEmpty()
            properties["$prefix.weightGrams"] = item.weightGrams?.toString().orEmpty()
            properties["$prefix.available"] = item.available.toString()
        }

        snapshot.importedRoute?.let { route ->
            properties["route.present"] = "true"
            properties["route.routeName"] = route.routeName
            properties["route.fileName"] = route.fileName
            properties["route.distanceKm"] = route.distanceKm.toString()
            properties["route.ascentMeters"] = route.ascentMeters.toString()
            properties["route.status"] = route.status.name
            properties["route.pointCount"] = route.pointCount.toString()
        }

        properties["history.count"] = snapshot.historicalActivities.size.toString()
        snapshot.historicalActivities.forEachIndexed { index, activity ->
            val prefix = "history.$index"
            properties["$prefix.routeName"] = activity.routeName
            properties["$prefix.distanceKm"] = activity.distanceKm.toString()
            properties["$prefix.ascentMeters"] = activity.ascentMeters.toString()
            properties["$prefix.durationMinutes"] = activity.durationMinutes.toString()
        }

        return StringWriter().use { writer ->
            properties.store(writer, "TrailMate local snapshot")
            writer.toString()
        }
    }

    fun decode(raw: String): TrailMateSnapshot {
        if (raw.isBlank()) {
            return TrailMateSnapshot()
        }

        return runCatching {
            val properties = Properties().apply {
                load(StringReader(raw))
            }

            TrailMateSnapshot(
                profile = properties.decodeProfile(),
                inventory = properties.decodeInventory(),
                importedRoute = properties.decodeImportedRoute(),
                historicalActivities = properties.decodeHistoricalActivities()
            )
        }.getOrDefault(TrailMateSnapshot())
    }

    private fun Properties.decodeProfile(): BaselineProfile? {
        if (getProperty("profile.present") != "true") {
            return null
        }

        val exerciseFrequency = enumValue<ExerciseFrequency>("profile.exerciseFrequency") ?: return null
        val typicalDuration = enumValue<TypicalDuration>("profile.typicalDuration") ?: return null
        val experienceLevel = enumValue<ExperienceLevel>("profile.experienceLevel") ?: return null
        val ascentExperience = enumValue<AscentExperience>("profile.ascentExperience") ?: return null

        return BaselineProfile(
            exerciseFrequency = exerciseFrequency,
            typicalDuration = typicalDuration,
            experienceLevel = experienceLevel,
            ascentExperience = ascentExperience,
            heightCm = nullableInt("profile.heightCm"),
            weightKg = nullableInt("profile.weightKg"),
            commonPackWeightKg = nullableInt("profile.commonPackWeightKg")
        )
    }

    private fun Properties.decodeInventory(): GearInventory {
        val count = getProperty("inventory.count")?.toIntOrNull()
            ?: return TrailMateSnapshot().inventory
        val items = (0 until count).mapNotNull { index ->
            val prefix = "inventory.$index"
            val id = getProperty("$prefix.id")?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val category = getProperty("$prefix.category")?.takeIf { it.isNotBlank() } ?: return@mapNotNull null

            GearItem(
                id = id,
                category = category,
                brand = getProperty("$prefix.brand").orEmpty().ifBlank { null },
                model = getProperty("$prefix.model").orEmpty().ifBlank { null },
                weightGrams = nullableInt("$prefix.weightGrams"),
                available = getProperty("$prefix.available")?.toBooleanStrictOrNull() ?: true
            )
        }

        return GearInventory(items = items)
    }

    private fun Properties.decodeImportedRoute(): ImportedRoute? {
        if (getProperty("route.present") != "true") {
            return null
        }

        val routeName = getProperty("route.routeName")?.takeIf { it.isNotBlank() } ?: return null
        val fileName = getProperty("route.fileName")?.takeIf { it.isNotBlank() } ?: return null
        val distanceKm = getProperty("route.distanceKm")?.toDoubleOrNull() ?: return null
        val ascentMeters = getProperty("route.ascentMeters")?.toIntOrNull() ?: return null
        val status = enumValue<RouteImportStatus>("route.status") ?: return null
        val pointCount = getProperty("route.pointCount")?.toIntOrNull() ?: 0

        return ImportedRoute(
            routeName = routeName,
            fileName = fileName,
            distanceKm = distanceKm,
            ascentMeters = ascentMeters,
            status = status,
            pointCount = pointCount
        )
    }

    private fun Properties.decodeHistoricalActivities(): List<HistoricalActivity> {
        val count = getProperty("history.count")?.toIntOrNull() ?: return emptyList()

        return (0 until count).mapNotNull { index ->
            val prefix = "history.$index"
            val routeName = getProperty("$prefix.routeName")?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val distanceKm = getProperty("$prefix.distanceKm")?.toDoubleOrNull() ?: return@mapNotNull null
            val ascentMeters = getProperty("$prefix.ascentMeters")?.toIntOrNull() ?: return@mapNotNull null
            val durationMinutes = getProperty("$prefix.durationMinutes")?.toIntOrNull() ?: return@mapNotNull null

            HistoricalActivity(
                routeName = routeName,
                distanceKm = distanceKm,
                ascentMeters = ascentMeters,
                durationMinutes = durationMinutes
            )
        }
    }

    private inline fun <reified T : Enum<T>> Properties.enumValue(key: String): T? =
        getProperty(key)?.let { value ->
            runCatching { enumValueOf<T>(value) }.getOrNull()
        }

    private fun Properties.nullableInt(key: String): Int? =
        getProperty(key).orEmpty().takeIf { it.isNotBlank() }?.toIntOrNull()
}
