package com.yolo.fun_habits.framework.presentation.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yolo.fun_habits.business.data.util.GenericErrors
import com.yolo.fun_habits.business.domain.state.DataState
import com.yolo.fun_habits.business.domain.state.DataStateManager
import com.yolo.fun_habits.business.domain.state.MessageType
import com.yolo.fun_habits.business.domain.state.Response
import com.yolo.fun_habits.business.domain.state.StateEvent
import com.yolo.fun_habits.business.domain.state.StateMessage
import com.yolo.fun_habits.business.domain.state.UIComponentType
import com.yolo.fun_habits.framework.util.printLogD
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@FlowPreview
@ExperimentalCoroutinesApi
abstract class BaseViewModel<ViewState> : ViewModel() {
    private val _viewState: MutableLiveData<ViewState> = MutableLiveData()

    val dataStateManager: DataStateManager<ViewState> = object : DataStateManager<ViewState>() {

        override fun handleNewData(data: ViewState) {
            this@BaseViewModel.handleNewData(data)
        }
    }

    val viewState: LiveData<ViewState>
        get() = _viewState

    val shouldDisplayProgressBar: LiveData<Boolean> = dataStateManager.shouldDisplayProgressBar

    val stateMessage: LiveData<StateMessage?>
        get() = dataStateManager.messageStack.stateMessage

    // FOR DEBUGGING
    fun getMessageStackSize(): Int {
        return dataStateManager.messageStack.size
    }

    fun setupDataStateManager() = dataStateManager.setup()

    abstract fun handleNewData(data: ViewState)

    abstract fun setStateEvent(stateEvent: StateEvent)

    fun emitStateMessageEvent(
        stateMessage: StateMessage,
        stateEvent: StateEvent
    ) = flow {
        emit(
            DataState.error<ViewState>(
                response = stateMessage.response,
                stateEvent = stateEvent
            )
        )
    }

    fun emitInvalidStateEvent(stateEvent: StateEvent) = flow {
        emit(
            DataState.error<ViewState>(
                response = Response(
                    message = GenericErrors.INVALID_STATE_EVENT,
                    uiComponentType = UIComponentType.None,
                    messageType = MessageType.Error
                ),
                stateEvent = stateEvent
            )
        )
    }

    fun launchJob(
        stateEvent: StateEvent,
        jobFunction: Flow<DataState<ViewState>?>
    ) = dataStateManager.launchJob(stateEvent, jobFunction)

    fun getCurrentViewStateOrNew(): ViewState {
        return viewState.value ?: initNewViewState()
    }

    fun setViewState(viewState: ViewState) {
        _viewState.value = viewState
    }

    fun clearStateMessage(index: Int = 0) {
        printLogD("BaseViewModel", "clearStateMessage")
        dataStateManager.clearStateMessage(index)
    }

    fun clearActiveStateEvents() = dataStateManager.clearActiveStateEventCounter()

    fun clearAllStateMessages() = dataStateManager.clearAllStateMessages()

    fun printStateMessages() = dataStateManager.printStateMessages()

    fun cancelActiveJobs() = dataStateManager.cancelJobs()

    abstract fun initNewViewState(): ViewState
}
