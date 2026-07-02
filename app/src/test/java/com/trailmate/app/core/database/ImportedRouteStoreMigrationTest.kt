package com.trailmate.app.core.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ImportedRouteStoreMigrationTest {
    @Test
    fun missingMigrationFailsInsteadOfDroppingImportedRoutes() {
        val error = assertThrows(IllegalStateException::class.java) {
            SqliteImportedRouteStoreSchema.requireExplicitMigration(
                oldVersion = 2,
                newVersion = 3,
            )
        }

        assertEquals(
            "Missing TrailMate database migration from 2 to 3.",
            error.message,
        )
    }
}
