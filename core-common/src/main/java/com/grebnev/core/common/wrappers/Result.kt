package com.grebnev.core.common.wrappers

sealed class Result<out T> {
    data class Success<out T>(
        val data: T,
    ) : Result<T>()

    data class Error(
        val exception: Throwable,
    ) : Result<Nothing>()

    data object Loading : Result<Nothing>()

    data object Empty : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success<T>
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading
    val isEmpty: Boolean get() = this is Empty

    companion object {
        fun <T> success(data: T): Result<T> = Success(data)

        fun error(exception: Throwable): Result<Nothing> = Error(exception)

        fun loading(): Result<Nothing> = Loading

        fun empty(): Result<Nothing> = Empty
    }
}