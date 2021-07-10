package com.yolo.fun_habit_journal.business.data.network.util

import com.yolo.fun_habit_journal.business.data.util.GenericErrors
import com.yolo.fun_habit_journal.framework.util.crashliticsLogs
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeNetworkCall(
    dispatcher: CoroutineDispatcher,
    networkCall: suspend () -> T?
): NetworkResult<T?> {
    return withContext(dispatcher) {
        try {
            withTimeout(NetworkConstants.NETWORK_TIMEOUT) {
                NetworkResult.Success(networkCall.invoke())
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            crashliticsLogs("network" + throwable.message)
            when (throwable) {
                is TimeoutCancellationException -> {
                    val code = 408 // timeout error code
                    NetworkResult.GenericError(code, NetworkErrors.NETWORK_ERROR_TIMEOUT)
                }
                is IOException -> {
                    NetworkResult.NetworkError
                }
                is HttpException -> {
                    val code = throwable.code()
                    val errorResponse = convertErrorBody(throwable)
                    crashliticsLogs(errorResponse)
                    NetworkResult.GenericError(
                        code,
                        errorResponse
                    )
                }
                else -> {
                    crashliticsLogs(NetworkErrors.NETWORK_ERROR_UNKNOWN)
                    NetworkResult.GenericError(
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

