package com.yolo.fun_habits.dependencyinjection

import androidx.lifecycle.ViewModelProvider
import com.yolo.fun_habits.business.domain.model.HabitFactory
import com.yolo.fun_habits.business.usecases.habitdetail.HabitDetailInteractors
import com.yolo.fun_habits.business.usecases.habitlist.HabitListInteractors
import com.yolo.fun_habits.framework.presentation.common.HabitViewModelFactory
import com.yolo.fun_habits.framework.presentation.splash.HabitNetworkSyncManager
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@FlowPreview
@Module
object HabitViewModelModule {

    @Singleton
    @JvmStatic
    @Provides
    fun provideHabitViewModelFactory(
        habitListInteractors: HabitListInteractors,
        habitDetailInteractors: HabitDetailInteractors,
        habitFactory: HabitFactory,
        habitNetworkSyncManager: HabitNetworkSyncManager
    ): ViewModelProvider.Factory {
        return HabitViewModelFactory(
            habitListInteractors = habitListInteractors,
            habitDetailInteractors = habitDetailInteractors,
            habitFactory = habitFactory,
            habitNetworkSyncManager = habitNetworkSyncManager
        )
    }

}
