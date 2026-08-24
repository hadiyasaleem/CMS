package com.mbd.cmscommon.util

sealed interface Outcome<out T> {
    data object Loading : Outcome<Nothing>
    data class Success<T>(val data: T) : Outcome<T>
    data class Error(val message: String, val cause: Throwable? = null) : Outcome<Nothing>
}

inline fun <T, R> Outcome<T>.map(transform: (T) -> R): Outcome<R> = when (this) {
    is Outcome.Loading -> Outcome.Loading
    is Outcome.Error -> this
    is Outcome.Success -> Outcome.Success(transform(data))
}
