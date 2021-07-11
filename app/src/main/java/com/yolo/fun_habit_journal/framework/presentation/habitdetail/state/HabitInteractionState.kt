package com.yolo.fun_habit_journal.framework.presentation.habitdetail.state



sealed class HabitInteractionState {

    class EditState: HabitInteractionState() {

        override fun toString(): String {
            return "EditState"
        }
    }

    class DefaultState: HabitInteractionState(){

        override fun toString(): String {
            return "DefaultState"
        }
    }
}
