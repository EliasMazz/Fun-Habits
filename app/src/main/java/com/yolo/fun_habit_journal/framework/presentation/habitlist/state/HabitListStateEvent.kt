package com.yolo.fun_habit_journal.framework.presentation.habitlist.state

import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.state.StateEvent
import com.yolo.fun_habit_journal.business.domain.state.StateMessage

sealed class HabitListStateEvent: StateEvent {

    object GetHabitsLisEvent : HabitListStateEvent() {

        override fun errorInfo(): String {
            return "Error getting list of habits."
        }

        override fun eventName(): String {
            return "GetHabitsListEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class InsertNewHabitEvent(
        val title: String
    ): HabitListStateEvent() {

        override fun errorInfo(): String {
            return "Error inserting new habit."
        }

        override fun eventName(): String {
            return "InsertNewHabitEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class RestoreDeletedHabitEvent(
        val habit: Habit
    ): HabitListStateEvent() {

        override fun errorInfo(): String {
            return "Error restoring the habit that was deleted."
        }

        override fun eventName(): String {
            return "RestoreDeletedHabitEvent"
        }

        override fun shouldDisplayProgressBar() = false
    }


    object GetHabitsCountInCacheEvent : HabitListStateEvent() {

        override fun errorInfo(): String {
            return "Error getting the number of habits from the cache."
        }

        override fun eventName(): String {
            return "GetNumHabitsInCacheEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class CreateStateMessageEvent(
        val stateMessage: StateMessage
    ): HabitListStateEvent(){

        override fun errorInfo(): String {
            return "Error creating a new state message."
        }

        override fun eventName(): String {
            return "CreateStateMessageEvent"
        }

        override fun shouldDisplayProgressBar() = false
    }
}
