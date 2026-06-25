package com.trailmate.app.feature.home

data class DataControlClearUiState(
    val isConfirmingClear: Boolean = false
) {
    fun requestClear(): DataControlClearUiState =
        copy(isConfirmingClear = true)

    fun cancelClear(): DataControlClearUiState =
        copy(isConfirmingClear = false)

    fun confirmClear(onClearLocalData: () -> Unit): DataControlClearUiState {
        if (isConfirmingClear) {
            onClearLocalData()
        }

        return copy(isConfirmingClear = false)
    }
}
