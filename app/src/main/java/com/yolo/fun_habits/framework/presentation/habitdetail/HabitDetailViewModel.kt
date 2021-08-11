package com.yolo.fun_habits.framework.presentation.habitdetail

import com.yolo.fun_habits.business.domain.model.Habit
import com.yolo.fun_habits.business.domain.state.DataState
import com.yolo.fun_habits.business.domain.state.MessageType
import com.yolo.fun_habits.business.domain.state.Response
import com.yolo.fun_habits.business.domain.state.StateEvent
import com.yolo.fun_habits.business.domain.state.StateMessage
import com.yolo.fun_habits.business.domain.state.UIComponentType
import com.yolo.fun_habits.business.usecases.habitdetail.HabitDetailInteractors
import com.yolo.fun_habits.business.usecases.habitdetail.usecase.UPDATE_HABIT_FAILED
import com.yolo.fun_habits.framework.datasource.cache.model.HabitCacheEntity
import com.yolo.fun_habits.framework.presentation.common.BaseViewModel
import com.yolo.fun_habits.framework.presentation.habitdetail.state.HabitDetailStateEvent
import com.yolo.fun_habits.framework.presentation.habitdetail.state.HabitDetailViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

const val HABIT_DETAIL_STATE_RESTORED = "State restored"
const val HABIT_DETAIL_ERROR_RETRIEVEING_SELECTED_HABIT = "Error retrieving selected habit from bundle."
@ExperimentalCoroutinesApi
@FlowPreview
class HabitDetailViewModel
@Inject
constructor(
    private val habitDetailInteractors: HabitDetailInteractors
) : BaseViewModel<HabitDetailViewState>() {

    override fun handleNewData(data: HabitDetailViewState) {
        // no data coming in from requests...
    }

    override fun setStateEvent(stateEvent: StateEvent) {
        val job: Flow<DataState<HabitDetailViewState>?> = when (stateEvent) {

            is HabitDetailStateEvent.UpdateHabitEvent -> {
                val id = getHabit()?.id
                if (!isHabitTitleNull() && id != null) {
                    habitDetailInteractors.updateHabitUseCase.invoke(
                        habit = getHabit()!!,
                        stateEvent = stateEvent
                    )
                } else {
                    emitStateMessageEvent(
                        stateMessage = StateMessage(
                            response = Response(
                                message = UPDATE_HABIT_FAILED,
                                uiComponentType = UIComponentType.Dialog,
                                messageType = MessageType.Error
                            )
                        ),
                        stateEvent = stateEvent
                    )
                }
            }

            is HabitDetailStateEvent.DeleteHabitEvent -> {
                habitDetailInteractors.deleteHabitUseCase.invoke(
                    habit = stateEvent.habit,
                    stateEvent = stateEvent
                )
            }

            is HabitDetailStateEvent.CreateStateMessageEvent -> {
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

    fun beginPendingDelete(habit: Habit) {
        setStateEvent(
            HabitDetailStateEvent.DeleteHabitEvent(
                habit = habit
            )
        )
    }

    private fun isHabitTitleNull(): Boolean {
        val title = getHabit()?.title
        if (title.isNullOrBlank()) {
            setStateEvent(
                HabitDetailStateEvent.CreateStateMessageEvent(
                    stateMessage = StateMessage(
                        response = Response(
                            message = HABIT_TITLE_CANNOT_BE_EMPTY,
                            uiComponentType = UIComponentType.Dialog,
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

    fun getHabit(): Habit? {
        return getCurrentViewStateOrNew().habit
    }

    override fun initNewViewState(): HabitDetailViewState {
        return HabitDetailViewState()
    }

    fun setHabit(habit: Habit?) {
        val update = getCurrentViewStateOrNew()
        update.habit = habit
        setViewState(update)
    }

    fun updateHabit(title: String?, body: String?) {
        if (title == null) {
            setStateEvent(
                HabitDetailStateEvent.CreateStateMessageEvent(
                    stateMessage = StateMessage(
                        response = Response(
                            message = HabitCacheEntity.nullTitleError(),
                            uiComponentType = UIComponentType.Dialog,
                            messageType = MessageType.Error
                        )
                    )
                )
            )
        } else {
            val update = getCurrentViewStateOrNew()
            val updatedNote = update.habit?.copy(
                title = title,
                body = body ?: ""
            )
            update.habit = updatedNote
            setViewState(update)
        }
    }
}
