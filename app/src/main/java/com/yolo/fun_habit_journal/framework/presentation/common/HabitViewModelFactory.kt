package com.yolo.fun_habit_journal.framework.presentation.common

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yolo.fun_habit_journal.business.domain.model.HabitFactory
import com.yolo.fun_habit_journal.business.usecases.habitdetail.HabitDetailInteractors
import com.yolo.fun_habit_journal.business.usecases.habitlist.HabitListInteractors
import com.yolo.fun_habit_journal.framework.presentation.habitdetail.HabitDetailViewModel
import com.yolo.fun_habit_journal.framework.presentation.habitlist.HabitListViewModel
import com.yolo.fun_habit_journal.framework.presentation.splash.SplashViewModel
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
    private val editor: SharedPreferences.Editor,
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            HabitListViewModel::class.java -> {
                HabitListViewModel(
                    habitListInteractors = habitListInteractors,
                    habitFactory = habitFactory,
                    editor = editor,
                    sharedPreferences = sharedPreferences
                ) as T
            }

            HabitDetailViewModel::class.java -> {
                HabitDetailViewModel(
                    habitDetailInteractors = habitDetailInteractors
                ) as T
            }

            SplashViewModel::class.java -> {
                SplashViewModel() as T
            }

            else -> {
                throw IllegalArgumentException("unknown model class $modelClass")
            }
        }
    }
}
