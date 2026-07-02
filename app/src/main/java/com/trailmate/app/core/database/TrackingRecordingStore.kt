package com.trailmate.app.core.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.trailmate.app.core.geo.LocationSample
import com.trailmate.app.core.model.Elevation
import com.trailmate.app.core.model.GeoCoordinate
import com.trailmate.app.core.model.GpsAccuracy
import com.trailmate.app.core.model.NavigationDirection
import com.trailmate.app.core.model.NavigationSessionId
import com.trailmate.app.core.model.NavigationState
import com.trailmate.app.core.model.PrivacyVisibility
import com.trailmate.app.core.model.RouteId
import java.time.Instant

interface TrackingRecordingStore {
    fun upsertSession(record: TrackingSessionRecord)

    fun appendSample(
        sessionId: NavigationSessionId,
        sample: LocationSample,
    ): TrackingTrackPointRecord

    fun findActiveSession(): TrackingSessionRecord?

    fun loadPoints(sessionId: NavigationSessionId): List<TrackingTrackPointRecord>

    fun markSessionEnded(
        sessionId: NavigationSessionId,
        endedAt: Instant,
    )
}

class SqliteTrackingRecordingStore(
    context: Context,
) : SQLiteOpenHelper(
    context,
    TrailMateDatabaseSchema.DatabaseName,
    null,
    TrailMateDatabaseSchema.DatabaseVersion,
),
    TrackingRecordingStore {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        TrailMateDatabaseSchema.createAll(db)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    ) {
        TrailMateDatabaseSchema.migrate(db, oldVersion, newVersion)
    }

    override fun upsertSession(record: TrackingSessionRecord) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val existingPointCount = db.countPoints(record.sessionId)
            val storedRecord = record.copy(
                sampleCount = record.sampleCount.coerceAtLeast(existingPointCount),
            )
            val insertedRowId = db.insertWithOnConflict(
                SessionsTable,
                null,
                storedRecord.toContentValues(),
                SQLiteDatabase.CONFLICT_IGNORE,
            )
            if (insertedRowId == -1L) {
                db.update(
                    SessionsTable,
                    storedRecord.toContentValues(),
                    "session_id = ?",
                    arrayOf(record.sessionId.value),
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override fun appendSample(
        sessionId: NavigationSessionId,
        sample: LocationSample,
    ): TrackingTrackPointRecord {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val record = TrackingTrackPointRecord.fromSample(
                sessionId = sessionId,
                pointIndex = db.nextPointIndex(sessionId),
                sample = sample,
            )
            db.insertOrThrow(
                PointsTable,
                null,
                record.toContentValues(),
            )
            db.update(
                SessionsTable,
                ContentValues().apply {
                    put(
                        "sample_count",
                        db.countPoints(record.sessionId),
                    )
                },
                "session_id = ?",
                arrayOf(record.sessionId.value),
            )
            db.setTransactionSuccessful()
            return record
        } finally {
            db.endTransaction()
        }
    }

    override fun findActiveSession(): TrackingSessionRecord? =
        readableDatabase.query(
            SessionsTable,
            SessionColumns,
            "ended_at_epoch_millis IS NULL",
            null,
            null,
            null,
            "started_at_epoch_millis DESC",
            "1",
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.toSessionRecord() else null
        }

    override fun loadPoints(sessionId: NavigationSessionId): List<TrackingTrackPointRecord> =
        readableDatabase.query(
            PointsTable,
            PointColumns,
            "session_id = ?",
            arrayOf(sessionId.value),
            null,
            null,
            "point_index ASC",
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.toTrackPointRecord())
                }
            }
        }

    override fun markSessionEnded(
        sessionId: NavigationSessionId,
        endedAt: Instant,
    ) {
        writableDatabase.update(
            SessionsTable,
            ContentValues().apply {
                put("ended_at_epoch_millis", endedAt.toEpochMilli())
                put("state", NavigationState.Ended.name)
            },
            "session_id = ?",
            arrayOf(sessionId.value),
        )
    }

    private fun SQLiteDatabase.countPoints(sessionId: NavigationSessionId): Int =
        rawQuery(
            "SELECT COUNT(*) FROM $PointsTable WHERE session_id = ?",
            arrayOf(sessionId.value),
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }

    private fun SQLiteDatabase.nextPointIndex(sessionId: NavigationSessionId): Int =
        rawQuery(
            "SELECT COALESCE(MAX(point_index) + 1, 0) FROM $PointsTable WHERE session_id = ?",
            arrayOf(sessionId.value),
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }

    private fun TrackingSessionRecord.toContentValues(): ContentValues =
        ContentValues().apply {
            put("session_id", sessionId.value)
            put("route_id", routeId.value)
            put("started_at_epoch_millis", startedAtEpochMillis)
            endedAtEpochMillis?.let { put("ended_at_epoch_millis", it) } ?: putNull("ended_at_epoch_millis")
            put("state", state.name)
            put("direction", direction.name)
            put("visibility", visibility.name)
            put("sample_count", sampleCount)
        }

    private fun TrackingTrackPointRecord.toContentValues(): ContentValues =
        ContentValues().apply {
            put("session_id", sessionId.value)
            put("point_index", pointIndex)
            put("latitude", coordinate.latitude)
            put("longitude", coordinate.longitude)
            coordinate.elevation?.meters?.let { put("elevation_meters", it) } ?: putNull("elevation_meters")
            put("accuracy_meters", accuracy.meters)
            put("recorded_at_epoch_millis", recordedAtEpochMillis)
            bearingDegrees?.let { put("bearing_degrees", it) } ?: putNull("bearing_degrees")
            speedMetersPerSecond?.let { put("speed_meters_per_second", it) } ?: putNull("speed_meters_per_second")
        }

    private fun Cursor.toSessionRecord(): TrackingSessionRecord {
        val visibility = enumValueOrNull<PrivacyVisibility>(getString("visibility")) ?: PrivacyVisibility.Private
        return TrackingSessionRecord(
            sessionId = NavigationSessionId(getString("session_id")),
            routeId = RouteId(getString("route_id")),
            startedAtEpochMillis = getLong("started_at_epoch_millis"),
            endedAtEpochMillis = getNullableLong("ended_at_epoch_millis"),
            state = enumValueOrNull<NavigationState>(getString("state")) ?: NavigationState.Idle,
            direction = enumValueOrNull<NavigationDirection>(getString("direction")) ?: NavigationDirection.Forward,
            visibility = visibility,
            sampleCount = getInt("sample_count"),
        )
    }

    private fun Cursor.toTrackPointRecord(): TrackingTrackPointRecord =
        TrackingTrackPointRecord(
            sessionId = NavigationSessionId(getString("session_id")),
            pointIndex = getInt("point_index"),
            coordinate = GeoCoordinate(
                latitude = getDouble("latitude"),
                longitude = getDouble("longitude"),
                elevation = getNullableDouble("elevation_meters")?.let(Elevation::meters),
            ),
            accuracy = GpsAccuracy(getDouble("accuracy_meters")),
            recordedAtEpochMillis = getLong("recorded_at_epoch_millis"),
            bearingDegrees = getNullableDouble("bearing_degrees"),
            speedMetersPerSecond = getNullableDouble("speed_meters_per_second"),
        )

    private fun Cursor.getString(columnName: String): String =
        getString(getColumnIndexOrThrow(columnName))

    private fun Cursor.getInt(columnName: String): Int =
        getInt(getColumnIndexOrThrow(columnName))

    private fun Cursor.getLong(columnName: String): Long =
        getLong(getColumnIndexOrThrow(columnName))

    private fun Cursor.getNullableLong(columnName: String): Long? {
        val index = getColumnIndexOrThrow(columnName)
        return if (isNull(index)) null else getLong(index)
    }

    private fun Cursor.getDouble(columnName: String): Double =
        getDouble(getColumnIndexOrThrow(columnName))

    private fun Cursor.getNullableDouble(columnName: String): Double? {
        val index = getColumnIndexOrThrow(columnName)
        return if (isNull(index)) null else getDouble(index)
    }

    private inline fun <reified T : Enum<T>> enumValueOrNull(value: String): T? =
        enumValues<T>().firstOrNull { it.name == value }

    private companion object {
        const val SessionsTable = TrailMateDatabaseSchema.TrackingSessionsTable
        const val PointsTable = TrailMateDatabaseSchema.TrackingTrackPointsTable

        val SessionColumns = arrayOf(
            "session_id",
            "route_id",
            "started_at_epoch_millis",
            "ended_at_epoch_millis",
            "state",
            "direction",
            "visibility",
            "sample_count",
        )

        val PointColumns = arrayOf(
            "session_id",
            "point_index",
            "latitude",
            "longitude",
            "elevation_meters",
            "accuracy_meters",
            "recorded_at_epoch_millis",
            "bearing_degrees",
            "speed_meters_per_second",
        )
    }
}

object SqliteTrackingRecordingStoreSchema {
    fun requireExplicitMigration(
        oldVersion: Int,
        newVersion: Int,
    ) {
        TrailMateDatabaseSchema.requireExplicitMigration(oldVersion, newVersion)
    }
}
