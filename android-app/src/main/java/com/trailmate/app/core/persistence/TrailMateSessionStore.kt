package com.trailmate.app.core.persistence

import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.ImportedRoute

interface TrailMateSessionStore {
    fun load(): TrailMateSnapshot

    fun saveProfile(profile: BaselineProfile)

    fun saveInventory(inventory: GearInventory)

    fun saveImportedRoute(route: ImportedRoute)
}
