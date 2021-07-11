package com.yolo.fun_habit_journal.dependencyinjection

import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import com.yolo.fun_habit_journal.business.domain.model.HabitFactory
import com.yolo.fun_habit_journal.business.usecases.habitdetail.HabitDetailInteractors
import com.yolo.fun_habit_journal.business.usecases.habitlist.HabitListInteractors
import com.yolo.fun_habit_journal.framework.presentation.common.HabitViewModelFactory
import com.yolo.fun_habit_journal.framework.presentation.splash.HabitNetworkSyncManager
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
        editor: SharedPreferences.Editor,
        sharedPreferences: SharedPreferences,
        habitNetworkSyncManager: HabitNetworkSyncManager
    ): ViewModelProvider.Factory {
        return HabitViewModelFactory(
            habitListInteractors = habitListInteractors,
            habitDetailInteractors = habitDetailInteractors,
            habitFactory = habitFactory,
            editor = editor,
            sharedPreferences = sharedPreferences,
            habitNetworkSyncManager = habitNetworkSyncManager
        )
    }

}
