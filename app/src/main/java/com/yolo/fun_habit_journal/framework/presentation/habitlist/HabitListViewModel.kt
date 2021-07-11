package com.yolo.fun_habit_journal.framework.presentation.habitlist

import android.content.SharedPreferences
import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.model.HabitFactory
import com.yolo.fun_habit_journal.business.domain.state.DataState
import com.yolo.fun_habit_journal.business.domain.state.MessageType
import com.yolo.fun_habit_journal.business.domain.state.Response
import com.yolo.fun_habit_journal.business.domain.state.StateEvent
import com.yolo.fun_habit_journal.business.domain.state.StateMessage
import com.yolo.fun_habit_journal.business.domain.state.UIComponentType
import com.yolo.fun_habit_journal.business.usecases.habitlist.HabitListInteractors
import com.yolo.fun_habit_journal.business.usecases.habitlist.usecase.DELETE_HABITS_YOU_MUST_SELECT
import com.yolo.fun_habit_journal.framework.datasource.database.HABIT_FILTER_DATE_CREATED
import com.yolo.fun_habit_journal.framework.datasource.database.HABIT_ORDER_DESC
import com.yolo.fun_habit_journal.framework.datasource.preferences.PreferenceKeys.Companion.HABIT_FILTER
import com.yolo.fun_habit_journal.framework.datasource.preferences.PreferenceKeys.Companion.HABIT_ORDER
import com.yolo.fun_habit_journal.framework.presentation.common.BaseViewModel
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListInteractionManager
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListStateEvent.*
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListToolbarState
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListViewState
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListViewState.*
import com.yolo.fun_habit_journal.framework.util.printLogD
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

const val DELETE_PENDING_ERROR = "There is already a pending delete operation."

@ExperimentalCoroutinesApi
@FlowPreview
class HabitListViewModel
constructor(
    private val habitListInteractors: HabitListInteractors,
    private val habitFactory: HabitFactory,
    private val editor: SharedPreferences.Editor,
    private val sharedPreferences: SharedPreferences
) : BaseViewModel<HabitListViewState>() {

    // -------------- TODO Check if necessary ------------------------------------------------------------
    val habitListInteractionManager =
        HabitListInteractionManager()

    val toolbarState: LiveData<HabitListToolbarState>
        get() = habitListInteractionManager.toolbarState

    fun getSelectedHabits() = habitListInteractionManager.getSelectedHabits()

    fun setToolbarState(state: HabitListToolbarState) = habitListInteractionManager.setToolbarState(state)

    fun isMultiSelectionStateActive() = habitListInteractionManager.isMultiSelectionStateActive()

    fun addOrRemoveHabitFromSelectedList(habit: Habit) = habitListInteractionManager.addOrRemoveHabitFromSelectedList(habit)

    fun isHabitSelected(habit: Habit): Boolean = habitListInteractionManager.isHabitSelected(habit)

    fun clearSelectedHabits() = habitListInteractionManager.clearSelectedHabits()


    private fun removeSelectedHabitsFromList() {
        val update = getCurrentViewStateOrNew()
        update.habitList?.removeAll(getSelectedHabits())
        setViewState(update)
        clearSelectedHabits()
    }

    fun deleteNotes() {
        if (getSelectedHabits().size > 0) {
            setStateEvent(DeleteMultipleHabitsEvent(getSelectedHabits()))
            removeSelectedHabitsFromList()
        } else {
            setStateEvent(
                CreateStateMessageEvent(
                    stateMessage = StateMessage(
                        response = Response(
                            message = DELETE_HABITS_YOU_MUST_SELECT,
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Info
                        )
                    )
                )
            )
        }
    }

    // ------------------------------------------------------------

    init {
        setHabitFilter(
            sharedPreferences.getString(
                HABIT_FILTER,
                HABIT_FILTER_DATE_CREATED
            )
        )
        setHabitOrder(
            sharedPreferences.getString(
                HABIT_ORDER,
                HABIT_ORDER_DESC
            )
        )
    }

    override fun handleNewData(data: HabitListViewState) {
        data.let { viewState ->
            viewState.habitList?.let { habitList ->
                setHabitListData(habitList)
            }

            viewState.habitsCountInCache?.let { habitListCount ->
                setHabitListCountInCache(habitListCount)
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

            is DeleteHabitEvent -> {
                habitListInteractors.deleteHabitUseCase.deleteHabit(
                    habit = stateEvent.habit,
                    stateEvent = stateEvent
                )
            }

            is DeleteMultipleHabitsEvent -> {
                habitListInteractors.deleteMultipleHabitsUseCase.deleteHabits(
                    habitList = stateEvent.habits,
                    stateEvent = stateEvent
                )
            }

            is RestoreDeletedHabitEvent -> {
                habitListInteractors.restoreDeletedHabitUseCase.restoreDeletedHabit(
                    habit = stateEvent.habit,
                    stateEvent = stateEvent
                )
            }

            is SearchHabitsEvent -> {
                if (stateEvent.clearLayoutManagerState) {
                    clearLayoutManagerState()
                }

                habitListInteractors.searchHabitsUseCase.searchHabits(
                    query = getSearchQuery(),
                    filterAndOrder = getOrder() + getFilter(),
                    page = getPage(),
                    stateEvent = stateEvent
                )
            }

            is GetHabitsCountInCacheEvent -> {
                habitListInteractors.getHabitsCountUseCase.getHabitsCount(
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
    fun getFilter(): String {
        return getCurrentViewStateOrNew().filter
            ?: HABIT_FILTER_DATE_CREATED
    }

    fun getOrder(): String {
        return getCurrentViewStateOrNew().order
            ?: HABIT_ORDER_DESC
    }

    fun getSearchQuery(): String {
        return getCurrentViewStateOrNew().searchQuery
            ?: return ""
    }

    private fun getPage(): Int {
        return getCurrentViewStateOrNew().page
            ?: return 1
    }

    fun getHabitListSize() = getCurrentViewStateOrNew().habitList?.size ?: 0

    private fun getHabitListCountInCache() = getCurrentViewStateOrNew().habitsCountInCache ?: 0

    // for debugging
    fun getActiveJobs() = dataChannelManager.getActiveJobs()

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

    fun isPaginationExhausted() = getHabitListSize() >= getHabitListCountInCache()

    fun isQueryExhausted(): Boolean {
        printLogD(
            "HabitListViewModel",
            "is query exhasuted? ${getCurrentViewStateOrNew().isQueryExhausted ?: true}"
        )
        return getCurrentViewStateOrNew().isQueryExhausted ?: true
    }

    override fun initNewViewState(): HabitListViewState {
        return HabitListViewState()
    }

    /*
        Setters
     */
    private fun setHabitListData(habitList: ArrayList<Habit>) {
        val update = getCurrentViewStateOrNew()
        update.habitList = habitList
        setViewState(update)
    }

    fun setQueryExhausted(isExhausted: Boolean) {
        val update = getCurrentViewStateOrNew()
        update.isQueryExhausted = isExhausted
        setViewState(update)
    }

    // can be selected from Recyclerview or created new from dialog
    fun setHabit(habit: Habit?) {
        val update = getCurrentViewStateOrNew()
        update.newHabit = habit
        setViewState(update)
    }

    fun setQuery(query: String?) {
        val update = getCurrentViewStateOrNew()
        update.searchQuery = query
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

    private fun setHabitListCountInCache(habitListCount: Int) {
        val update = getCurrentViewStateOrNew()
        update.habitsCountInCache = habitListCount
        setViewState(update)
    }

    fun createNewHabit(
        id: String? = null,
        title: String,
        body: String? = null
    ) = habitFactory.createSingleHabit(id, title, body)

    private fun resetPage() {
        val update = getCurrentViewStateOrNew()
        update.page = 1
        setViewState(update)
    }

    fun clearList() {
        printLogD("ListViewModel", "clearList")
        val update = getCurrentViewStateOrNew()
        update.habitList = ArrayList()
        setViewState(update)
    }

    // workaround for tests
    // can't submit an empty string because SearchViews SUCK
    @VisibleForTesting
    fun clearSearchQuery() {
        setQuery("")
        clearList()
        loadFirstPage()
    }

    private fun incrementPageNumber() {
        val update = getCurrentViewStateOrNew()
        val page = update.copy().page ?: 1
        update.page = page.plus(1)
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

    fun setHabitFilter(filter: String?) {
        filter?.let {
            val update = getCurrentViewStateOrNew()
            update.filter = filter
            setViewState(update)
        }
    }

    fun setHabitOrder(order: String?) {
        val update = getCurrentViewStateOrNew()
        update.order = order
        setViewState(update)
    }

    fun saveFilterOptions(filter: String, order: String) {
        editor.putString(HABIT_FILTER, filter)
        editor.apply()

        editor.putString(HABIT_FILTER, order)
        editor.apply()
    }

    /*
        StateEvent Triggers
     */
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
        // replace habit in viewstate
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

    fun beginPendingDelete(habit: Habit) {
        setHabitPendingDelete(habit)
        removePendingHabitFromList(habit)
        setStateEvent(
            DeleteHabitEvent(
                habit = habit
            )
        )
    }

    fun loadFirstPage() {
        setQueryExhausted(false)
        resetPage()
        setStateEvent(SearchHabitsEvent())
        printLogD(
            "HabitListViewModel",
            "loadFirstPage: ${getCurrentViewStateOrNew().searchQuery}"
        )
    }

    fun nextPage() {
        if (!isQueryExhausted()) {
            printLogD("HabitListViewModel", "attempting to load next page...")
            clearLayoutManagerState()
            incrementPageNumber()
            setStateEvent(SearchHabitsEvent())
        }
    }

    fun retrieveHabitListCountInCache() {
        setStateEvent(GetHabitsCountInCacheEvent)
    }

    fun refreshSearchQuery() {
        setQueryExhausted(false)
        setStateEvent(SearchHabitsEvent(false))
    }
}
