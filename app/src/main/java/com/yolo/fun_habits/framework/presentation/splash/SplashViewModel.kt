package com.yolo.fun_habits.framework.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SplashViewModel
@Inject
constructor(
    private val habitNetworkSyncManager: HabitNetworkSyncManager
): ViewModel(){

    init {
        syncCacheWithNetwork()
    }

    fun hasSyncBeenExecuted() = habitNetworkSyncManager.hasSyncBeenExecuted

    private fun syncCacheWithNetwork() {
        habitNetworkSyncManager.executeDataSync(viewModelScope)
    }
}
