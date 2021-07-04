package com.yolo.fun_habit_journal.business.data.network.util

import com.yolo.fun_habit_journal.business.data.util.GenericErrors
import com.yolo.fun_habit_journal.framework.util.cLog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> T?
): ApiResult<T?> {
    return withContext(dispatcher) {
        try {
            // throws TimeoutCancellationException
            withTimeout(NetworkConstants.NETWORK_TIMEOUT) {
                ApiResult.Success(apiCall.invoke())
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            when (throwable) {
                is TimeoutCancellationException -> {
                    val code = 408 // timeout error code
                    ApiResult.GenericError(code, NetworkErrors.NETWORK_ERROR_TIMEOUT)
                }
                is IOException -> {
                    ApiResult.NetworkError
                }
                is HttpException -> {
                    val code = throwable.code()
                    val errorResponse = convertErrorBody(throwable)
                    cLog(errorResponse)
                    ApiResult.GenericError(
                        code,
                        errorResponse
                    )
                }
                else -> {
                    cLog(NetworkErrors.NETWORK_ERROR_UNKNOWN)
                    ApiResult.GenericError(
                        null,
                        NetworkErrors.NETWORK_ERROR_UNKNOWN
                    )
                }
            }
        }
    }
}

private fun convertErrorBody(throwable: HttpException): String? {
    return try {
        throwable.response()?.errorBody()?.string()
    } catch (exception: Exception) {
        GenericErrors.ERROR_UNKNOWN
    }
}

