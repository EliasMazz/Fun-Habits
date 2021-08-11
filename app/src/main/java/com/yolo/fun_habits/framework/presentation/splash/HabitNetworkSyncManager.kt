package com.yolo.fun_habits.framework.presentation.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.yolo.fun_habits.business.usecases.appstart.usecase.SyncDeletedHabitsUseCase
import com.yolo.fun_habits.business.usecases.appstart.usecase.SyncHabitsUseCase
import com.yolo.fun_habits.framework.util.printLogD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitNetworkSyncManager
@Inject
constructor(
    private val syncHabits: SyncHabitsUseCase,
    private val syncDeletedHabits: SyncDeletedHabitsUseCase
){
    private val _hasSyncBeenExecuted: MutableLiveData<Boolean> = MutableLiveData(false)

    val hasSyncBeenExecuted: LiveData<Boolean>
        get() = _hasSyncBeenExecuted

    fun executeDataSync(coroutineScope: CoroutineScope){
        if(_hasSyncBeenExecuted.value!!){
            return
        }

        val syncJob = coroutineScope.launch {
            launch {
                printLogD("SyncDeletedHabits", "syncing deleted habits.")
                syncDeletedHabits.invoke()
            }.join()

            launch {
                printLogD("SyncNotes", "syncing habits.")
                syncHabits.invoke()
            }
        }
        syncJob.invokeOnCompletion {
            CoroutineScope(Main).launch{
                _hasSyncBeenExecuted.value = true
            }
        }
    }

}


