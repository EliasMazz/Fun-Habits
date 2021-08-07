package com.yolo.fun_habits.business.data.cache.util

import com.yolo.fun_habits.framework.util.crashliticsLogs
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

suspend fun <T> safeCacheCall(
    dispatcher: CoroutineDispatcher,
    cacheCall: suspend () -> T?
): CacheResult<T?> {
    return withContext(dispatcher) {
        try {
            withTimeout(CacheConstants.CACHE_TIMEOUT) {
                CacheResult.Success(cacheCall.invoke())
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            crashliticsLogs("cache" + throwable.message)
            when (throwable) {
                is TimeoutCancellationException -> {
                    CacheResult.GenericError(CacheErrors.CACHE_ERROR_TIMEOUT)
                }
                else -> {
                    crashliticsLogs(CacheErrors.CACHE_ERROR_UNKNOWN)
                    CacheResult.GenericError(CacheErrors.CACHE_ERROR_UNKNOWN)
                }
            }
        }
    }
}
