package com.trailmate.app.feature.gear

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GearCatalogSourceUiStateTest {
    @Test
    fun localPreviewDescribesOfflinePackagedCatalogSeed() {
        val state = GearCatalogSourceUiState.localPreview()

        assertEquals("离线品牌库", state.label)
        assertTrue(state.caption.contains("离线可用"))
        assertTrue(state.caption.contains("联网版本"))
        assertFalse(state.caption.contains("服务端品牌库缓存"))
        assertFalse(state.isLoading)
        assertFalse(state.canRetry)
    }

    @Test
    fun loadingLabelsServerSync() {
        val state = GearCatalogSourceUiState.loading()

        assertEquals("同步品牌库", state.label)
        assertTrue(state.caption.contains("正在从服务端同步"))
        assertTrue(state.isLoading)
        assertFalse(state.canRetry)
    }

    @Test
    fun serverSyncedIncludesItemCount() {
        val state = GearCatalogSourceUiState.serverSynced(itemCount = 12)

        assertEquals("服务端品牌库", state.label)
        assertTrue(state.caption.contains("已同步 12 件品牌装备"))
        assertFalse(state.isLoading)
        assertFalse(state.canRetry)
    }

    @Test
    fun fallbackCacheAllowsRetry() {
        val state = GearCatalogSourceUiState.fallbackCache()

        assertEquals("品牌库缓存", state.label)
        assertTrue(state.caption.contains("稍后可重试同步"))
        assertFalse(state.isLoading)
        assertTrue(state.canRetry)
    }
}
