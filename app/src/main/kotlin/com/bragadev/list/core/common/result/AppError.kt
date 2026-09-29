package com.bragadev.list.core.common.result

/**
 * Application-level error taxonomy. Data sources translate technical exceptions
 * (SQLite exceptions today, IOException/HttpException once networking is added)
 * into this sealed type so the UI never has to handle raw exceptions.
 */
sealed class AppError {
    data object Network : AppError()

    data object Timeout : AppError()

    data class HttpClient(val code: Int) : AppError()

    data class HttpServer(val code: Int) : AppError()

    data object Parsing : AppError()

    data object Database : AppError()

    data class Validation(val reason: String) : AppError()

    data class Unknown(val message: String? = null) : AppError()
}
