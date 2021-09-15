package com.kneelawk.ledcontroller1

inline fun <R, T : R> Result<T>.getOrDefaultElse(default: R, onFailure: (Throwable) -> Unit): R {
    return getOrElse {
        onFailure(it)
        default
    }
}
