package com.yolo.fun_habits.business.data.cache.util

import com.yolo.fun_habits.business.domain.state.DataState
import com.yolo.fun_habits.business.domain.state.MessageType
import com.yolo.fun_habits.business.domain.state.Response
import com.yolo.fun_habits.business.domain.state.StateEvent
import com.yolo.fun_habits.business.domain.state.UIComponentType

abstract class CacheResultHandler<ViewState, Data>(
    private val response: CacheResult<Data?>,
    private val stateEvent: StateEvent?
) {
    suspend fun getResult(): DataState<ViewState>? {
        return when (response) {
            is CacheResult.GenericError -> {
                DataState.error(
                    response = Response(
                        message = "${stateEvent?.errorInfo()} Reason: ${response.errorMessage}",
                        uiComponentType = UIComponentType.Dialog,
                        messageType = MessageType.Error
                    ), stateEvent = stateEvent
                )
            }
            is CacheResult.Success -> {
                if (response.value == null) {
                    return DataState.error(
                        response = Response(
                            message = "${stateEvent?.errorInfo()} Reason: ${CacheErrors.CACHE_DATA_NULL}",
                            uiComponentType = UIComponentType.Dialog,
                            messageType = MessageType.Error
                        ), stateEvent = stateEvent
                    )
                } else {
                    handleDataState(result = response.value)
                }
            }
        }
    }

    abstract suspend fun handleDataState(result: Data): DataState<ViewState>?
}
