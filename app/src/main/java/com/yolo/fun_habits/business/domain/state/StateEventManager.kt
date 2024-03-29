package com.yolo.fun_habits.business.domain.state

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.yolo.fun_habits.framework.util.printLogD

/**
 * - Keeps track of active StateEvents in DataStateManager
 * - Keeps track of whether the progress bar should show or not based on a boolean
 *      value in each StateEvent (shouldDisplayProgressBar)
 */
class StateEventManager {

    private val activeStateEvents: HashMap<String, StateEvent> = HashMap()

    private val _shouldDisplayProgressBar: MutableLiveData<Boolean> = MutableLiveData()

    val shouldDisplayProgressBar: LiveData<Boolean>
        get() = _shouldDisplayProgressBar

    fun getActiveJobNames(): MutableSet<String>{
        return activeStateEvents.keys
    }

    fun clearActiveStateEventCounter(){
        printLogD("DCM", "Clear active state events")
        activeStateEvents.clear()
        syncNumActiveStateEvents()
    }

    fun addStateEvent(stateEvent: StateEvent){
        printLogD("DCM sem", "Add event: ${stateEvent?.eventName()}")
        activeStateEvents.put(stateEvent.eventName(), stateEvent)
        syncNumActiveStateEvents()
    }

    fun removeStateEvent(stateEvent: StateEvent?){
        printLogD("DCM sem", "remove state event: ${stateEvent?.eventName()}")
        activeStateEvents.remove(stateEvent?.eventName())
        syncNumActiveStateEvents()
    }

    fun isStateEventActive(stateEvent: StateEvent): Boolean{
        printLogD("DCM sem", "is state event active? " + "${activeStateEvents.containsKey(stateEvent.eventName())}")
        for(eventName in activeStateEvents.keys){
            if(stateEvent.eventName().equals(eventName)){
                return true
            }
        }
        return false
    }

    private fun syncNumActiveStateEvents(){
        var shouldDisplayProgressBar = false
        for(stateEvent in activeStateEvents.values){
            if(stateEvent.shouldDisplayProgressBar()){
                shouldDisplayProgressBar = true
            }
        }
        _shouldDisplayProgressBar.value = shouldDisplayProgressBar
    }
}

