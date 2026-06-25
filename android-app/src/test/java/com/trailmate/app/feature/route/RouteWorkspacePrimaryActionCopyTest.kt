package com.trailmate.app.feature.route

import org.junit.Assert.assertEquals
import org.junit.Test

class RouteWorkspacePrimaryActionCopyTest {
    @Test
    fun importedRoutePrimaryActionOpensRouteAndDepartureCheck() {
        assertEquals("查看路线与出发检查", routeWorkspacePrimaryActionLabel(hasRoute = true))
    }

    @Test
    fun emptyRoutePrimaryActionImportsGpx() {
        assertEquals("导入 GPX 文件", routeWorkspacePrimaryActionLabel(hasRoute = false))
    }
}
