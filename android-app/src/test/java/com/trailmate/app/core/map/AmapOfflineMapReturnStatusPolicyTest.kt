package com.trailmate.app.core.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AmapOfflineMapReturnStatusPolicyTest {
    @Test
    fun showsNoDownloadDetectedAfterReturningWithoutDownloadedOrPendingChanges() {
        val result = AmapOfflineMapReturnStatusPolicy.resolve(
            returnedFromOfflineManager = true,
            beforeStatus = emptyStatus,
            afterStatus = emptyStatus,
            targetRegionLabel = "杭州市"
        )

        assertEquals(AmapOfflineMapReturnStatusAction.SHOW_NO_DOWNLOAD_DETECTED, result.action)
        assertTrue(result.message.orEmpty().contains("未检测到离线底图下载任务"))
        assertTrue(result.message.orEmpty().contains("杭州市"))
    }

    @Test
    fun doesNothingWhenOfflineManagerWasNotOpened() {
        val result = AmapOfflineMapReturnStatusPolicy.resolve(
            returnedFromOfflineManager = false,
            beforeStatus = emptyStatus,
            afterStatus = emptyStatus,
            targetRegionLabel = null
        )

        assertEquals(AmapOfflineMapReturnStatusAction.NONE, result.action)
        assertNull(result.message)
    }

    @Test
    fun clearsMessageWhenPendingDownloadAppearsAfterReturn() {
        val result = AmapOfflineMapReturnStatusPolicy.resolve(
            returnedFromOfflineManager = true,
            beforeStatus = emptyStatus,
            afterStatus = emptyStatus.copy(
                pendingCities = listOf(
                    AmapOfflineBaseMapPendingRegion(
                        name = "杭州市",
                        code = "0571",
                        adcode = "330100",
                        completePercent = 35,
                        stateLabel = "下载中"
                    )
                )
            ),
            targetRegionLabel = "杭州市"
        )

        assertEquals(AmapOfflineMapReturnStatusAction.CLEAR_RETURN_MESSAGE, result.action)
        assertNull(result.message)
    }

    @Test
    fun clearsMessageWhenDownloadedRegionAppearsAfterReturn() {
        val result = AmapOfflineMapReturnStatusPolicy.resolve(
            returnedFromOfflineManager = true,
            beforeStatus = emptyStatus,
            afterStatus = emptyStatus.copy(
                downloadedCities = listOf(
                    AmapOfflineBaseMapRegion(
                        name = "杭州市",
                        code = "0571",
                        adcode = "330100",
                        level = AmapOfflineBaseMapRegionLevel.CITY
                    )
                )
            ),
            targetRegionLabel = "杭州市"
        )

        assertEquals(AmapOfflineMapReturnStatusAction.CLEAR_RETURN_MESSAGE, result.action)
        assertNull(result.message)
    }

    private companion object {
        val emptyStatus = AmapOfflineBaseMapStatus(
            downloadedCities = emptyList(),
            downloadedProvinces = emptyList()
        )
    }
}
