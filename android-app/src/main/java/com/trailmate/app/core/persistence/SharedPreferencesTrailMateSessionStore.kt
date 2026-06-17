package com.trailmate.app.core.persistence

import android.content.Context
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.ImportedRoute

class SharedPreferencesTrailMateSessionStore(context: Context) : TrailMateSessionStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    override fun load(): TrailMateSnapshot =
        preferences.getString(KEY_SNAPSHOT, null)
            ?.let(TrailMateSnapshotCodec::decode)
            ?: TrailMateSnapshot()

    override fun saveProfile(profile: BaselineProfile) {
        update { snapshot -> snapshot.copy(profile = profile) }
    }

    override fun saveInventory(inventory: GearInventory) {
        update { snapshot -> snapshot.copy(inventory = inventory) }
    }

    override fun saveImportedRoute(route: ImportedRoute) {
        update { snapshot -> snapshot.copy(importedRoute = route) }
    }

    private fun update(transform: (TrailMateSnapshot) -> TrailMateSnapshot) {
        val updated = transform(load())
        preferences.edit()
            .putString(KEY_SNAPSHOT, TrailMateSnapshotCodec.encode(updated))
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "trailmate_session"
        const val KEY_SNAPSHOT = "snapshot"
    }
}
