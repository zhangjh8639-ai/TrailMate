package com.trailmate.app.feature.profile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountLogoutUiStateTest {
    @Test
    fun requestLogoutShowsConfirmationBeforeSigningOut() {
        val state = AccountLogoutUiState()

        val confirming = state.requestLogout()

        assertFalse(state.isConfirmingLogout)
        assertTrue(confirming.isConfirmingLogout)
    }

    @Test
    fun cancelLogoutReturnsToIdleWithoutSigningOut() {
        var logoutCount = 0
        val state = AccountLogoutUiState(isConfirmingLogout = true)

        val idle = state.cancelLogout()

        assertFalse(idle.isConfirmingLogout)
        assertEquals(0, logoutCount)
    }

    @Test
    fun confirmLogoutSignsOutOnceAndReturnsToIdle() {
        var logoutCount = 0
        val state = AccountLogoutUiState(isConfirmingLogout = true)

        val idle = state.confirmLogout {
            logoutCount += 1
        }

        assertFalse(idle.isConfirmingLogout)
        assertEquals(1, logoutCount)
    }

    @Test
    fun confirmLogoutFromIdleDoesNotSignOut() {
        var logoutCount = 0
        val state = AccountLogoutUiState()

        val idle = state.confirmLogout {
            logoutCount += 1
        }

        assertFalse(idle.isConfirmingLogout)
        assertEquals(0, logoutCount)
    }
}
