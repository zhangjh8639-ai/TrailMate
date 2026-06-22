package com.trailmate.app.core.map

import org.junit.Assert.assertEquals
import org.junit.Test

class AmapCameraFitPolicyTest {
    @Test
    fun fitsRouteBoundsTheFirstTimeGeometryIsRendered() {
        val result = AmapCameraFitPolicy.resolve(
            state = AmapCameraFitState(),
            routeKey = "longjing|15.2|42",
            hasRouteGeometry = true
        )

        assertEquals(AmapCameraFitDecision.FIT_ROUTE_BOUNDS, result.decision)
        assertEquals("longjing|15.2|42", result.state.fittedRouteKey)
    }

    @Test
    fun keepsCameraWhenSameRouteRefreshesWithNewOverlays() {
        val result = AmapCameraFitPolicy.resolve(
            state = AmapCameraFitState(fittedRouteKey = "longjing|15.2|42"),
            routeKey = "longjing|15.2|42",
            hasRouteGeometry = true
        )

        assertEquals(AmapCameraFitDecision.KEEP_CAMERA, result.decision)
        assertEquals("longjing|15.2|42", result.state.fittedRouteKey)
    }

    @Test
    fun fitsAgainWhenTheRouteChanges() {
        val result = AmapCameraFitPolicy.resolve(
            state = AmapCameraFitState(fittedRouteKey = "longjing|15.2|42"),
            routeKey = "wugong|18.0|68",
            hasRouteGeometry = true
        )

        assertEquals(AmapCameraFitDecision.FIT_ROUTE_BOUNDS, result.decision)
        assertEquals("wugong|18.0|68", result.state.fittedRouteKey)
    }

    @Test
    fun keepsCameraWhenRouteGeometryIsMissing() {
        val result = AmapCameraFitPolicy.resolve(
            state = AmapCameraFitState(fittedRouteKey = "longjing|15.2|42"),
            routeKey = "broken|0|0",
            hasRouteGeometry = false
        )

        assertEquals(AmapCameraFitDecision.KEEP_CAMERA, result.decision)
        assertEquals("longjing|15.2|42", result.state.fittedRouteKey)
    }
}
