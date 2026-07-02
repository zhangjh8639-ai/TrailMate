package com.trailmate.app.core.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ImportedRouteStoreMigrationTest {
    @Test
    fun missingMigrationFailsInsteadOfDroppingImportedRoutes() {
        val error = assertThrows(IllegalStateException::class.java) {
            SqliteImportedRouteStoreSchema.requireExplicitMigration(
                oldVersion = 1,
                newVersion = 2,
            )
        }

        assertEquals(
            "Missing imported route database migration from 1 to 2.",
            error.message,
        )
    }
}
