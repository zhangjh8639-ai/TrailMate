package com.trailmate.app.core.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.trailmate.app.core.model.PrivacyVisibility
import com.trailmate.app.core.model.RouteConfidence
import com.trailmate.app.core.model.RouteOfflineStatus
import com.trailmate.app.core.model.RouteSourceType

interface ImportedRouteStore {
    fun loadAll(): List<ImportedRouteRecord>

    fun upsert(record: ImportedRouteRecord)
}

class SqliteImportedRouteStore(
    context: Context,
) : SQLiteOpenHelper(
    context,
    DatabaseName,
    null,
    DatabaseVersion,
),
    ImportedRouteStore {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $RoutesTable (
                id TEXT PRIMARY KEY NOT NULL,
                file_name TEXT NOT NULL,
                source_type TEXT NOT NULL,
                route_name TEXT NOT NULL,
                distance_meters REAL NOT NULL,
                elevation_gain_meters REAL NOT NULL,
                waypoint_count INTEGER NOT NULL,
                track_point_count INTEGER NOT NULL,
                has_elevation INTEGER NOT NULL,
                imported_at_epoch_millis INTEGER NOT NULL,
                visibility TEXT NOT NULL,
                offline_status TEXT NOT NULL,
                confidence TEXT NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE $PointsTable (
                route_id TEXT NOT NULL,
                point_index INTEGER NOT NULL,
                latitude REAL NOT NULL,
                longitude REAL NOT NULL,
                elevation_meters REAL,
                cumulative_distance_meters REAL NOT NULL,
                PRIMARY KEY(route_id, point_index),
                FOREIGN KEY(route_id) REFERENCES $RoutesTable(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE INDEX index_${PointsTable}_route_id ON $PointsTable(route_id)",
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    ) {
        SqliteImportedRouteStoreSchema.requireExplicitMigration(oldVersion, newVersion)
    }

    override fun loadAll(): List<ImportedRouteRecord> {
        val db = readableDatabase
        return db.query(
            RoutesTable,
            RouteColumns,
            null,
            null,
            null,
            null,
            "imported_at_epoch_millis DESC",
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    cursor.toImportedRouteRecord(db)?.let(::add)
                }
            }
        }
    }

    override fun upsert(record: ImportedRouteRecord) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.insertWithOnConflict(
                RoutesTable,
                null,
                record.toRouteContentValues(),
                SQLiteDatabase.CONFLICT_REPLACE,
            )
            db.delete(PointsTable, "route_id = ?", arrayOf(record.id))
            record.points.forEach { point ->
                db.insertOrThrow(
                    PointsTable,
                    null,
                    point.toPointContentValues(routeId = record.id),
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun Cursor.toImportedRouteRecord(db: SQLiteDatabase): ImportedRouteRecord? {
        val id = getString("id")
        val sourceType = enumValueOrNull<RouteSourceType>(getString("source_type")) ?: return null
        val visibility = enumValueOrNull<PrivacyVisibility>(getString("visibility")) ?: return null
        val offlineStatus = enumValueOrNull<RouteOfflineStatus>(getString("offline_status")) ?: return null
        val confidence = enumValueOrNull<RouteConfidence>(getString("confidence")) ?: return null
        val points = db.loadPoints(routeId = id)
        if (points.size < 2) return null

        return ImportedRouteRecord(
            id = id,
            fileName = getString("file_name"),
            sourceType = sourceType,
            routeName = getString("route_name"),
            distanceMeters = getDouble("distance_meters"),
            elevationGainMeters = getDouble("elevation_gain_meters"),
            waypointCount = getInt("waypoint_count"),
            trackPointCount = getInt("track_point_count"),
            hasElevation = getInt("has_elevation") == 1,
            importedAtEpochMillis = getLong("imported_at_epoch_millis"),
            visibility = visibility,
            offlineStatus = offlineStatus,
            confidence = confidence,
            points = points,
        )
    }

    private fun SQLiteDatabase.loadPoints(routeId: String): List<ImportedRoutePointRecord> =
        query(
            PointsTable,
            PointColumns,
            "route_id = ?",
            arrayOf(routeId),
            null,
            null,
            "point_index ASC",
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        ImportedRoutePointRecord(
                            pointIndex = cursor.getInt("point_index"),
                            latitude = cursor.getDouble("latitude"),
                            longitude = cursor.getDouble("longitude"),
                            elevationMeters = cursor.getNullableDouble("elevation_meters"),
                            cumulativeDistanceMeters = cursor.getDouble("cumulative_distance_meters"),
                        ),
                    )
                }
            }
        }

    private fun ImportedRouteRecord.toRouteContentValues(): ContentValues =
        ContentValues().apply {
            put("id", id)
            put("file_name", fileName)
            put("source_type", sourceType.name)
            put("route_name", routeName)
            put("distance_meters", distanceMeters)
            put("elevation_gain_meters", elevationGainMeters)
            put("waypoint_count", waypointCount)
            put("track_point_count", trackPointCount)
            put("has_elevation", if (hasElevation) 1 else 0)
            put("imported_at_epoch_millis", importedAtEpochMillis)
            put("visibility", visibility.name)
            put("offline_status", offlineStatus.name)
            put("confidence", confidence.name)
        }

    private fun ImportedRoutePointRecord.toPointContentValues(routeId: String): ContentValues =
        ContentValues().apply {
            put("route_id", routeId)
            put("point_index", pointIndex)
            put("latitude", latitude)
            put("longitude", longitude)
            elevationMeters?.let { put("elevation_meters", it) } ?: putNull("elevation_meters")
            put("cumulative_distance_meters", cumulativeDistanceMeters)
        }

    private fun Cursor.getString(columnName: String): String =
        getString(getColumnIndexOrThrow(columnName))

    private fun Cursor.getInt(columnName: String): Int =
        getInt(getColumnIndexOrThrow(columnName))

    private fun Cursor.getLong(columnName: String): Long =
        getLong(getColumnIndexOrThrow(columnName))

    private fun Cursor.getDouble(columnName: String): Double =
        getDouble(getColumnIndexOrThrow(columnName))

    private fun Cursor.getNullableDouble(columnName: String): Double? {
        val index = getColumnIndexOrThrow(columnName)
        return if (isNull(index)) null else getDouble(index)
    }

    private inline fun <reified T : Enum<T>> enumValueOrNull(value: String): T? =
        enumValues<T>().firstOrNull { it.name == value }

    private companion object {
        const val DatabaseName = "trailmate.db"
        const val DatabaseVersion = 1
        const val RoutesTable = "imported_routes"
        const val PointsTable = "imported_route_points"

        val RouteColumns = arrayOf(
            "id",
            "file_name",
            "source_type",
            "route_name",
            "distance_meters",
            "elevation_gain_meters",
            "waypoint_count",
            "track_point_count",
            "has_elevation",
            "imported_at_epoch_millis",
            "visibility",
            "offline_status",
            "confidence",
        )

        val PointColumns = arrayOf(
            "point_index",
            "latitude",
            "longitude",
            "elevation_meters",
            "cumulative_distance_meters",
        )
    }
}

object SqliteImportedRouteStoreSchema {
    fun requireExplicitMigration(
        oldVersion: Int,
        newVersion: Int,
    ) {
        if (oldVersion == newVersion) return

        throw IllegalStateException(
            "Missing imported route database migration from $oldVersion to $newVersion.",
        )
    }
}
