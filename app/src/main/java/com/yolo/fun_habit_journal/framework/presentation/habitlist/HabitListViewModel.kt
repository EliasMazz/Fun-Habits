package com.yolo.fun_habit_journal.framework.presentation.habitlist

import android.content.SharedPreferences
import com.yolo.fun_habit_journal.business.domain.model.HabitFactory
import com.yolo.fun_habit_journal.business.domain.state.StateEvent
import com.yolo.fun_habit_journal.business.usecases.habitlist.HabitListInteractors
import com.yolo.fun_habit_journal.framework.presentation.common.BaseViewModel
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class HabitListViewModel
constructor(
    private val habitListInteractors: HabitListInteractors,
    private val habitFactory: HabitFactory,
    private val editor: SharedPreferences.Editor,
    private val sharedPreferences: SharedPreferences
): BaseViewModel<HabitListViewState>(){

    override fun handleNewData(data: HabitListViewState) {

    }

    override fun setStateEvent(stateEvent: StateEvent) {
    }

    override fun initNewViewState(): HabitListViewState {
        return HabitListViewState()
    }
}
