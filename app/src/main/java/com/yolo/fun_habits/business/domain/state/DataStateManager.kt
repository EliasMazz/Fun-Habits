package com.yolo.fun_habits.business.domain.state

import com.yolo.fun_habits.framework.util.printLogD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@FlowPreview
@ExperimentalCoroutinesApi
abstract class DataStateManager<ViewState> {
    private var coroutineScope: CoroutineScope? = null
    private val stateEventManager: StateEventManager = StateEventManager()

    val messageStack = MessageStack()

    val shouldDisplayProgressBar = stateEventManager.shouldDisplayProgressBar

    fun setup() {
        cancelJobs()
    }

    abstract fun handleNewData(data: ViewState)

    fun launchJob(
        stateEvent: StateEvent,
        jobFunction: Flow<DataState<ViewState>?>
    ) {
        if (canExecuteNewStateEvent(stateEvent)) {
            printLogD("DCM", "launching job: ${stateEvent.eventName()}")
            addStateEvent(stateEvent)
            jobFunction
                .onEach { dataState ->
                    dataState?.let { dState ->
                        withContext(Main) {
                            dataState.data?.let { data ->
                                handleNewData(data)
                            }
                            dataState.stateMessage?.let { stateMessage ->
                                handleNewStateMessage(stateMessage)
                            }
                            dataState.stateEvent?.let { stateEvent ->
                                removeStateEvent(stateEvent)
                            }
                        }
                    }
                }.launchIn(getScope())
        }
    }

    private fun canExecuteNewStateEvent(stateEvent: StateEvent): Boolean {
        // If a job is already active, do not allow duplication
        if (isJobAlreadyActive(stateEvent)) {
            return false
        }
        // Check the top of the stack, if a dialog is showing, do not allow new StateEvents
        if(!isMessageStackEmpty()){
            if(messageStack[0].response.uiComponentType == UIComponentType.Dialog){
                return false
            }
        }
        return true
    }

    fun isMessageStackEmpty(): Boolean {
        return messageStack.isStackEmpty()
    }

    private fun handleNewStateMessage(stateMessage: StateMessage) {
        appendStateMessage(stateMessage)
    }

    private fun appendStateMessage(stateMessage: StateMessage) {
        messageStack.add(stateMessage)
    }

    fun clearStateMessage(index: Int = 0) {
        printLogD("DataChannelManager", "clear state message")
        messageStack.removeAt(index)
    }

    fun clearAllStateMessages() = messageStack.clear()

    fun printStateMessages() {
        for (message in messageStack) {
            printLogD("DCM", "${message.response.message}")
        }
    }

    // for debugging
    fun getActiveJobs() = stateEventManager.getActiveJobNames()

    fun clearActiveStateEventCounter() = stateEventManager.clearActiveStateEventCounter()

    fun addStateEvent(stateEvent: StateEvent) = stateEventManager.addStateEvent(stateEvent)

    fun removeStateEvent(stateEvent: StateEvent?) = stateEventManager.removeStateEvent(stateEvent)

    private fun isStateEventActive(stateEvent: StateEvent) = stateEventManager.isStateEventActive(stateEvent)

    fun isJobAlreadyActive(stateEvent: StateEvent): Boolean {
        return isStateEventActive(stateEvent)
    }

    fun getScope(): CoroutineScope {
        return coroutineScope ?: setupNewScope(CoroutineScope(IO))
    }

    private fun setupNewScope(coroutineScope: CoroutineScope): CoroutineScope {
        this.coroutineScope = coroutineScope
        return this.coroutineScope as CoroutineScope
    }

    fun cancelJobs() {
        if (coroutineScope != null) {
            if (coroutineScope?.isActive == true) {
                coroutineScope?.cancel()
            }
            coroutineScope = null
        }
        clearActiveStateEventCounter()
    }

}












