package com.trailmate.app.core.map

enum class AmapCameraFitDecision {
    FIT_ROUTE_BOUNDS,
    KEEP_CAMERA
}

data class AmapCameraFitState(
    val fittedRouteKey: String? = null
)

data class AmapCameraFitResult(
    val state: AmapCameraFitState,
    val decision: AmapCameraFitDecision
)

object AmapCameraFitPolicy {
    fun resolve(
        state: AmapCameraFitState,
        routeKey: String,
        hasRouteGeometry: Boolean
    ): AmapCameraFitResult {
        if (!hasRouteGeometry) {
            return AmapCameraFitResult(
                state = state,
                decision = AmapCameraFitDecision.KEEP_CAMERA
            )
        }

        val shouldFit = state.fittedRouteKey != routeKey
        return AmapCameraFitResult(
            state = if (shouldFit) state.copy(fittedRouteKey = routeKey) else state,
            decision = if (shouldFit) {
                AmapCameraFitDecision.FIT_ROUTE_BOUNDS
            } else {
                AmapCameraFitDecision.KEEP_CAMERA
            }
        )
    }
}
