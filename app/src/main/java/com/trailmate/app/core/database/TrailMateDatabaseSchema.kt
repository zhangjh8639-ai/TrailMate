package com.trailmate.app.core.database

import android.database.sqlite.SQLiteDatabase

object TrailMateDatabaseSchema {
    const val DatabaseName = "trailmate.db"
    const val DatabaseVersion = 2

    const val ImportedRoutesTable = "imported_routes"
    const val ImportedRoutePointsTable = "imported_route_points"
    const val TrackingSessionsTable = "tracking_sessions"
    const val TrackingTrackPointsTable = "tracking_track_points"

    fun createAll(db: SQLiteDatabase) {
        createImportedRouteTables(db)
        createTrackingRecordingTables(db)
    }

    fun migrate(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    ) {
        if (oldVersion == 1 && newVersion == 2) {
            createAll(db)
            return
        }

        requireExplicitMigration(oldVersion, newVersion)
    }

    fun requireExplicitMigration(
        oldVersion: Int,
        newVersion: Int,
    ) {
        if (oldVersion == newVersion) return

        throw IllegalStateException(
            "Missing TrailMate database migration from $oldVersion to $newVersion.",
        )
    }

    private fun createImportedRouteTables(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $ImportedRoutesTable (
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
            CREATE TABLE IF NOT EXISTS $ImportedRoutePointsTable (
                route_id TEXT NOT NULL,
                point_index INTEGER NOT NULL,
                latitude REAL NOT NULL,
                longitude REAL NOT NULL,
                elevation_meters REAL,
                cumulative_distance_meters REAL NOT NULL,
                PRIMARY KEY(route_id, point_index),
                FOREIGN KEY(route_id) REFERENCES $ImportedRoutesTable(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_${ImportedRoutePointsTable}_route_id ON $ImportedRoutePointsTable(route_id)",
        )
    }

    private fun createTrackingRecordingTables(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TrackingSessionsTable (
                session_id TEXT PRIMARY KEY NOT NULL,
                route_id TEXT NOT NULL,
                started_at_epoch_millis INTEGER NOT NULL,
                ended_at_epoch_millis INTEGER,
                state TEXT NOT NULL,
                direction TEXT NOT NULL,
                visibility TEXT NOT NULL,
                sample_count INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TrackingTrackPointsTable (
                session_id TEXT NOT NULL,
                point_index INTEGER NOT NULL,
                latitude REAL NOT NULL,
                longitude REAL NOT NULL,
                elevation_meters REAL,
                accuracy_meters REAL NOT NULL,
                recorded_at_epoch_millis INTEGER NOT NULL,
                bearing_degrees REAL,
                speed_meters_per_second REAL,
                PRIMARY KEY(session_id, point_index),
                FOREIGN KEY(session_id) REFERENCES $TrackingSessionsTable(session_id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_${TrackingTrackPointsTable}_session_id ON $TrackingTrackPointsTable(session_id)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_${TrackingSessionsTable}_active ON $TrackingSessionsTable(ended_at_epoch_millis, started_at_epoch_millis)",
        )
    }
}
