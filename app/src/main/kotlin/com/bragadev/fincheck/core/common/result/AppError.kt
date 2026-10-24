package com.bragadev.fincheck.core.common.result

/**
 * Application-level error taxonomy. Data sources translate technical exceptions
 * (SQLite exceptions today, IOException/HttpException once networking is added)
 * into this sealed type so the UI never has to handle raw exceptions.
 */
sealed class AppError {
    /** No network connection (reserved for when the app talks to a server). */
    data object Network : AppError()

    /** The server took too long to answer (reserved for networking). */
    data object Timeout : AppError()

    /** The server rejected the request with a 4xx status [code]. */
    data class HttpClient(val code: Int) : AppError()

    /** The server failed with a 5xx status [code]. */
    data class HttpServer(val code: Int) : AppError()

    /** A response or stored value could not be read. */
    data object Parsing : AppError()

    /** Reading or writing the local Room database failed. */
    data object Database : AppError()

    /** Input broke a business rule; [reason] is a short machine-readable code such as "invalid_item_name". */
    data class Validation(val reason: String) : AppError()

    /** Any other failure; [message] is the original exception message, for logs only. */
    data class Unknown(val message: String? = null) : AppError()
}
