package com.yolo.fun_habit_journal.framework.presentation.habitlist.state

sealed class HabitListToolbarState {

    class MultiSelectionState : HabitListToolbarState() {

        override fun toString(): String {
            return "MultiSelectionState"
        }
    }

}
