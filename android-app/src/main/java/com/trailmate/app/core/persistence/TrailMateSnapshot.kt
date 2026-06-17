package com.trailmate.app.core.persistence

import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.TrailMateSampleData

data class TrailMateSnapshot(
    val profile: BaselineProfile? = null,
    val inventory: GearInventory = GearInventory(TrailMateSampleData.gearItems),
    val importedRoute: ImportedRoute? = null
)
