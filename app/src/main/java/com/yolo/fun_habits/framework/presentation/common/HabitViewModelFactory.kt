package com.yolo.fun_habits.framework.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yolo.fun_habits.business.domain.model.HabitFactory
import com.yolo.fun_habits.business.usecases.habitdetail.HabitDetailInteractors
import com.yolo.fun_habits.business.usecases.habitlist.HabitListInteractors
import com.yolo.fun_habits.framework.presentation.habitdetail.HabitDetailViewModel
import com.yolo.fun_habits.framework.presentation.habitlist.HabitListViewModel
import com.yolo.fun_habits.framework.presentation.splash.HabitNetworkSyncManager
import com.yolo.fun_habits.framework.presentation.splash.SplashViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("UNCHECKED_CAST")
@FlowPreview
@ExperimentalCoroutinesApi
@Singleton
class HabitViewModelFactory
@Inject
constructor(
    private val habitListInteractors: HabitListInteractors,
    private val habitDetailInteractors: HabitDetailInteractors,
    private val habitFactory: HabitFactory,
    private val habitNetworkSyncManager: HabitNetworkSyncManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            HabitListViewModel::class.java -> {
                HabitListViewModel(
                    habitListInteractors = habitListInteractors,
                    habitFactory = habitFactory
                ) as T
            }

            HabitDetailViewModel::class.java -> {
                HabitDetailViewModel(
                    habitDetailInteractors = habitDetailInteractors
                ) as T
            }

            SplashViewModel::class.java -> {
                SplashViewModel(habitNetworkSyncManager) as T
            }

            else -> {
                throw IllegalArgumentException("unknown model class $modelClass")
            }
        }
    }
}
