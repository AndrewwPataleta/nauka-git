package com.nauchat.core.ext

sealed class ViewDataState<out T : Any?> {
    data object Loading : ViewDataState<Nothing>()

    data class Error(
        val message: String?,
    ) : ViewDataState<Nothing>()

    data class Success<out T : Any?>(
        val data: T,
    ) : ViewDataState<T>()

    data object Empty : ViewDataState<Nothing>()
}
