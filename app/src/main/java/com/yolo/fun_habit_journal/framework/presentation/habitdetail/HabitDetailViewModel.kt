package com.yolo.fun_habit_journal.framework.presentation.habitdetail

import com.yolo.fun_habit_journal.business.domain.state.StateEvent
import com.yolo.fun_habit_journal.business.usecases.habitdetail.HabitDetailInteractors
import com.yolo.fun_habit_journal.framework.presentation.common.BaseViewModel
import com.yolo.fun_habit_journal.framework.presentation.habitdetail.state.HabitDetailViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class HabitDetailViewModel
@Inject
constructor(
    private val habitDetailInteractors: HabitDetailInteractors
) : BaseViewModel<HabitDetailViewState>() {

    override fun handleNewData(data: HabitDetailViewState) {

    }

    override fun setStateEvent(stateEvent: StateEvent) {

    }

    override fun initNewViewState(): HabitDetailViewState {
        return HabitDetailViewState()
    }
}
