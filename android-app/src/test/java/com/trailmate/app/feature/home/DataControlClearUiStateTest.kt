package com.trailmate.app.feature.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DataControlClearUiStateTest {
    @Test
    fun requestClearShowsConfirmationBeforeClearing() {
        val state = DataControlClearUiState()

        val confirming = state.requestClear()

        assertFalse(state.isConfirmingClear)
        assertTrue(confirming.isConfirmingClear)
    }

    @Test
    fun cancelClearReturnsToIdleWithoutClearing() {
        var clearCount = 0
        val state = DataControlClearUiState(isConfirmingClear = true)

        val idle = state.cancelClear()

        assertFalse(idle.isConfirmingClear)
        assertEquals(0, clearCount)
    }

    @Test
    fun confirmClearClearsOnceAndReturnsToIdle() {
        var clearCount = 0
        val state = DataControlClearUiState(isConfirmingClear = true)

        val idle = state.confirmClear {
            clearCount += 1
        }

        assertFalse(idle.isConfirmingClear)
        assertEquals(1, clearCount)
    }

    @Test
    fun confirmClearFromIdleDoesNotClear() {
        var clearCount = 0
        val state = DataControlClearUiState()

        val idle = state.confirmClear {
            clearCount += 1
        }

        assertFalse(idle.isConfirmingClear)
        assertEquals(0, clearCount)
    }
}
