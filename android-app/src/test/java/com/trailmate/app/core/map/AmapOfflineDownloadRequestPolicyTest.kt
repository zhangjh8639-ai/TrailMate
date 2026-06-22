package com.trailmate.app.core.map

import org.junit.Assert.assertEquals
import org.junit.Test

class AmapOfflineDownloadRequestPolicyTest {
    @Test
    fun prefersCityCodeWhenResolvedCatalogProvidesOne() {
        val request = AmapOfflineDownloadRequestPolicy.resolve(
            cityName = "杭州市",
            cityCode = "0571"
        )

        assertEquals(AmapOfflineDownloadRequestKind.CITY_CODE, request.kind)
        assertEquals("0571", request.value)
    }

    @Test
    fun fallsBackToCityNameWhenCityCodeIsMissing() {
        val request = AmapOfflineDownloadRequestPolicy.resolve(
            cityName = "杭州市",
            cityCode = null
        )

        assertEquals(AmapOfflineDownloadRequestKind.CITY_NAME, request.kind)
        assertEquals("杭州市", request.value)
    }
}
