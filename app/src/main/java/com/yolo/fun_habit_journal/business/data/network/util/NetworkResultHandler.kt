package com.yolo.fun_habit_journal.business.data.network.util

import com.yolo.fun_habit_journal.business.data.network.util.NetworkErrors.NETWORK_DATA_NULL
import com.yolo.fun_habit_journal.business.domain.state.DataState
import com.yolo.fun_habit_journal.business.domain.state.MessageType
import com.yolo.fun_habit_journal.business.domain.state.Response
import com.yolo.fun_habit_journal.business.domain.state.StateEvent
import com.yolo.fun_habit_journal.business.domain.state.UIComponentType

abstract class NetworkResultHandler<ViewState, Data>(
    private val response: NetworkResult<Data?>,
    private val stateEvent: StateEvent?
) {

    suspend fun getResult(): DataState<ViewState> {
        return when (response) {
            is NetworkResult.GenericError -> {
                DataState.error(
                    response = Response(
                        message = "${stateEvent?.errorInfo()} Reason: ${response.errorMessage}",
                        uiComponentType = UIComponentType.Dialog,
                        messageType = MessageType.Error
                    ), stateEvent = stateEvent
                )
            }

            is NetworkResult.NetworkError -> {
                DataState.error(
                    response = Response(
                        message = "${stateEvent?.errorInfo()} Reason: ${NetworkResult.NetworkError}",
                        uiComponentType = UIComponentType.Dialog,
                        messageType = MessageType.Error
                    ), stateEvent = stateEvent
                )
            }

            is NetworkResult.Success -> {
                if (response.value == null) {
                    return DataState.error(
                        response = Response(
                            message = "${stateEvent?.errorInfo()} Reason: ${NETWORK_DATA_NULL}",
                            uiComponentType = UIComponentType.Dialog,
                            messageType = MessageType.Error
                        ), stateEvent = stateEvent
                    )
                } else {
                    handleSuccess(result = response.value)
                }
            }
        }
    }

    abstract suspend fun handleSuccess(result: Data): DataState<ViewState>
}
