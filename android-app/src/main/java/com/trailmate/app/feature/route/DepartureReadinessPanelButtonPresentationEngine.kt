package com.trailmate.app.feature.route

import com.trailmate.app.core.model.DepartureReadinessPrimaryAction

data class DepartureReadinessPanelButtonPresentation(
    val label: String,
    val enabled: Boolean
)

object DepartureReadinessPanelButtonPresentationEngine {
    fun present(action: DepartureReadinessPrimaryAction): DepartureReadinessPanelButtonPresentation =
        DepartureReadinessPanelButtonPresentation(
            label = "下一步：${action.label}",
            enabled = action.enabled
        )
}
