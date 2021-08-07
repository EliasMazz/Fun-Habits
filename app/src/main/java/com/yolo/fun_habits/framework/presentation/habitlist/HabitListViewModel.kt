package com.yolo.fun_habits.framework.presentation.habitlist

import com.yolo.fun_habits.business.domain.model.Habit
import com.yolo.fun_habits.business.domain.model.HabitFactory
import com.yolo.fun_habits.business.domain.state.DataState
import com.yolo.fun_habits.business.domain.state.StateEvent
import com.yolo.fun_habits.business.usecases.habitlist.HabitListInteractors
import com.yolo.fun_habits.framework.presentation.common.BaseViewModel
import com.yolo.fun_habits.framework.presentation.habitlist.state.HabitListStateEvent.*
import com.yolo.fun_habits.framework.presentation.habitlist.state.HabitListViewState
import com.yolo.fun_habits.framework.util.printLogD
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

@ExperimentalCoroutinesApi
@FlowPreview
class HabitListViewModel
constructor(
    private val habitListInteractors: HabitListInteractors,
    private val habitFactory: HabitFactory
) : BaseViewModel<HabitListViewState>() {

    override fun handleNewData(data: HabitListViewState) {
        data.let { viewState ->
            viewState.habitList?.let { habitList ->
                setHabitListData(habitList)
            }

            viewState.newHabit?.let { habit ->
                setHabit(habit)
            }
        }

    }

    override fun setStateEvent(stateEvent: StateEvent) {
        val job: Flow<DataState<HabitListViewState>?> = when (stateEvent) {

            is InsertNewHabitEvent -> {
                habitListInteractors.insertNewHabitUseCase.insertNewHabit(
                    title = stateEvent.title,
                    stateEvent = stateEvent
                )
            }

            is GetHabitsLisEvent -> {
                habitListInteractors.getHabitstListUseCase.invoke(
                    stateEvent = stateEvent
                )
            }

            is CreateStateMessageEvent -> {
                emitStateMessageEvent(
                    stateMessage = stateEvent.stateMessage,
                    stateEvent = stateEvent
                )
            }

            else -> {
                emitInvalidStateEvent(stateEvent)
            }
        }
        launchJob(stateEvent, job)
    }


    // for debugging
    fun getActiveJobs() = dataStateManager.getActiveJobs()

    override fun initNewViewState(): HabitListViewState {
        return HabitListViewState()
    }

    private fun setHabitListData(habitList: ArrayList<Habit>) {
        val update = getCurrentViewStateOrNew()
        update.habitList = habitList
        setViewState(update)
    }


    // can be selected from Recyclerview or created new from dialog
    fun setHabit(habit: Habit?) {
        val update = getCurrentViewStateOrNew()
        update.newHabit = habit
        setViewState(update)
    }

    fun createNewHabit(
        id: String? = null,
        title: String,
        body: String? = null
    ) = habitFactory.createSingleHabit(id, title, body)


    fun clearList() {
        printLogD("ListViewModel", "clearList")
        val update = getCurrentViewStateOrNew()
        update.habitList = ArrayList()
        setViewState(update)
    }
}
