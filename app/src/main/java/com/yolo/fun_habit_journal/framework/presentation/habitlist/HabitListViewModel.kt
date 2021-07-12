package com.yolo.fun_habit_journal.framework.presentation.habitlist

import android.os.Parcelable
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.model.HabitFactory
import com.yolo.fun_habit_journal.business.domain.state.DataState
import com.yolo.fun_habit_journal.business.domain.state.MessageType
import com.yolo.fun_habit_journal.business.domain.state.Response
import com.yolo.fun_habit_journal.business.domain.state.StateEvent
import com.yolo.fun_habit_journal.business.domain.state.StateMessage
import com.yolo.fun_habit_journal.business.domain.state.UIComponentType
import com.yolo.fun_habit_journal.business.usecases.habitlist.HabitListInteractors
import com.yolo.fun_habit_journal.framework.presentation.common.BaseViewModel
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListStateEvent.*
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListViewState
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListViewState.*
import com.yolo.fun_habit_journal.framework.util.printLogD
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

const val DELETE_PENDING_ERROR = "There is already a pending delete operation."
const val HABIT_PENDING_DELETE_BUNDLE_KEY = "pending_delete"

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

            viewState.habitPendingDelete?.let { restoredHabit ->
                restoredHabit.habit?.let { habit ->
                    setRestoredHabitId(habit)
                }
                setHabitPendingDelete(null)
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

            is RestoreDeletedHabitEvent -> {
                habitListInteractors.restoreDeletedHabitUseCase.restoreDeletedHabit(
                    habit = stateEvent.habit,
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

    /*
        Getters
     */
    // for debugging
    fun getActiveJobs() = dataStateManager.getActiveJobs()

    fun getLayoutManagerState(): Parcelable? {
        return getCurrentViewStateOrNew().layoutManagerState
    }

    private fun findListPositionOfHabit(habit: Habit?): Int {
        val viewState = getCurrentViewStateOrNew()
        viewState.habitList?.let { habitList ->
            for ((index, item) in habitList.withIndex()) {
                if (item.id == habit?.id) {
                    return index
                }
            }
        }
        return 0
    }

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

    // if a habit is deleted and then restored, the id will be incorrect.
    // So need to reset it here.
    private fun setRestoredHabitId(restoredHabit: Habit) {
        val update = getCurrentViewStateOrNew()
        update.habitList?.let { habitList ->
            for ((index, habit) in habitList.withIndex()) {
                if (habit.title.equals(restoredHabit.title)) {
                    habitList.remove(habit)
                    habitList.add(index, restoredHabit)
                    update.habitList = habitList
                    break
                }
            }
        }
        setViewState(update)
    }

    private fun removePendingHabitFromList(habit: Habit?) {
        val update = getCurrentViewStateOrNew()
        val list = update.habitList
        if (list?.contains(habit) == true) {
            list.remove(habit)
            update.habitList = list
            setViewState(update)
        }
    }

    fun setHabitPendingDelete(habit: Habit?) {
        val update = getCurrentViewStateOrNew()
        if (habit != null) {
            update.habitPendingDelete = HabitPendingDelete(
                habit = habit,
                listPosition = findListPositionOfHabit(habit)
            )
        } else {
            update.habitPendingDelete = null
        }
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

    fun setLayoutManagerState(layoutManagerState: Parcelable) {
        val update = getCurrentViewStateOrNew()
        update.layoutManagerState = layoutManagerState
        setViewState(update)
    }

    fun clearLayoutManagerState() {
        val update = getCurrentViewStateOrNew()
        update.layoutManagerState = null
        setViewState(update)
    }

    fun isDeletePending(): Boolean {
        val pendingHabit = getCurrentViewStateOrNew().habitPendingDelete
        if (pendingHabit != null) {
            setStateEvent(
                CreateStateMessageEvent(
                    stateMessage = StateMessage(
                        response = Response(
                            message = DELETE_PENDING_ERROR,
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Info
                        )
                    )
                )
            )
            return true
        } else {
            return false
        }
    }

    fun undoDelete() {
        val update = getCurrentViewStateOrNew()
        update.habitPendingDelete?.let { habitPendingDelete ->
            if (habitPendingDelete.listPosition != null && habitPendingDelete.habit != null) {
                update.habitList?.add(
                    habitPendingDelete.listPosition as Int,
                    habitPendingDelete.habit as Habit
                )
                setStateEvent(RestoreDeletedHabitEvent(habitPendingDelete.habit as Habit))
            }
        }
        setViewState(update)
    }

}
