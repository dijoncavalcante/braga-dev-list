package com.bragadev.fincheck.core.common.result

/**
 * Domain-level result wrapper. Use cases and repositories return this instead of
 * throwing, so the presentation layer only ever deals with [AppError], never with
 * technical exceptions.
 */
sealed interface AppResult<out T> {
    /** The operation worked and produced [data]. */
    data class Success<T>(val data: T) : AppResult<T>

    /** The operation failed with [error]; nothing was thrown. */
    data class Error(val error: AppError) : AppResult<Nothing>
}

/** Runs [action] with the data when this is a [AppResult.Success]; returns this result unchanged, for chaining. */
inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) action(data)
    return this
}

/** Runs [action] with the error when this is an [AppResult.Error]; returns this result unchanged, for chaining. */
inline fun <T> AppResult<T>.onError(action: (AppError) -> Unit): AppResult<T> {
    if (this is AppResult.Error) action(error)
    return this
}
