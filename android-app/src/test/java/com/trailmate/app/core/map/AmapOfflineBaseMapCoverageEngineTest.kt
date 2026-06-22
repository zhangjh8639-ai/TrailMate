package com.trailmate.app.core.map

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmapOfflineBaseMapCoverageEngineTest {
    @Test
    fun downloadedCityCoversTargetRouteWhenCityAdcodeMatches() {
        val status = AmapOfflineBaseMapStatus(
            downloadedCities = listOf(
                AmapOfflineBaseMapRegion(
                    name = "杭州市",
                    code = "0571",
                    adcode = "330100",
                    level = AmapOfflineBaseMapRegionLevel.CITY
                )
            ),
            downloadedProvinces = emptyList()
        )
        val target = AmapTargetRouteRegion(
            provinceName = "浙江省",
            cityName = "杭州市",
            cityCode = "0571",
            adcode = "330100"
        )

        assertTrue(AmapOfflineBaseMapCoverageEngine.coversTargetRoute(status, target))
    }

    @Test
    fun downloadedProvinceCoversTargetRouteWhenProvinceMatches() {
        val status = AmapOfflineBaseMapStatus(
            downloadedCities = emptyList(),
            downloadedProvinces = listOf(
                AmapOfflineBaseMapRegion(
                    name = "浙江省",
                    code = "330000",
                    adcode = "330000",
                    level = AmapOfflineBaseMapRegionLevel.PROVINCE
                )
            )
        )
        val target = AmapTargetRouteRegion(
            provinceName = "浙江省",
            cityName = "杭州市",
            cityCode = "0571",
            adcode = "330100"
        )

        assertTrue(AmapOfflineBaseMapCoverageEngine.coversTargetRoute(status, target))
    }

    @Test
    fun downloadedDifferentCityDoesNotCoverTargetRoute() {
        val status = AmapOfflineBaseMapStatus(
            downloadedCities = listOf(
                AmapOfflineBaseMapRegion(
                    name = "北京市",
                    code = "010",
                    adcode = "110000",
                    level = AmapOfflineBaseMapRegionLevel.CITY
                )
            ),
            downloadedProvinces = emptyList()
        )
        val target = AmapTargetRouteRegion(
            provinceName = "浙江省",
            cityName = "杭州市",
            cityCode = "0571",
            adcode = "330100"
        )

        assertFalse(AmapOfflineBaseMapCoverageEngine.coversTargetRoute(status, target))
    }

    @Test
    fun missingTargetRegionDoesNotCoverRoute() {
        val status = AmapOfflineBaseMapStatus(
            downloadedCities = listOf(
                AmapOfflineBaseMapRegion(
                    name = "杭州市",
                    code = "0571",
                    adcode = "330100",
                    level = AmapOfflineBaseMapRegionLevel.CITY
                )
            ),
            downloadedProvinces = emptyList()
        )

        assertFalse(AmapOfflineBaseMapCoverageEngine.coversTargetRoute(status, null))
    }
}
