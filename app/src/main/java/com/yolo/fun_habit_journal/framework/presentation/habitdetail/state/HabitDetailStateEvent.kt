package com.yolo.fun_habit_journal.framework.presentation.habitdetail.state

import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.state.StateEvent
import com.yolo.fun_habit_journal.business.domain.state.StateMessage

sealed class HabitDetailStateEvent : StateEvent {

    class UpdateHabitEvent : HabitDetailStateEvent() {

        override fun errorInfo(): String {
            return "Error updating habit."
        }

        override fun eventName(): String {
            return "UpdateHabitEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class DeleteHabitEvent(
        val habit: Habit
    ) : HabitDetailStateEvent() {

        override fun errorInfo(): String {
            return "Error deleting habit"
        }

        override fun eventName(): String {
            return "DeleteHabitEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class CreateStateMessageEvent(
        val stateMessage: StateMessage
    ) : HabitDetailStateEvent() {

        override fun errorInfo(): String {
            return "Error creating a new state message."
        }

        override fun eventName(): String {
            return "CreateStateMessageEvent"
        }

        override fun shouldDisplayProgressBar() = false
    }

}
