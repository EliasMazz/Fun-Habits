package com.yolo.fun_habit_journal.framework.presentation.habitlist.state

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.yolo.fun_habit_journal.business.domain.model.Habit


class HabitListInteractionManager {

    private val _selectedHabits: MutableLiveData<ArrayList<Habit>> = MutableLiveData()

    val selectedHabits: LiveData<ArrayList<Habit>>
        get() = _selectedHabits


    fun getSelectedHabits(): ArrayList<Habit> = _selectedHabits.value ?: ArrayList()

    fun isMultiSelectionStateActive(): Boolean {
        return false
    }

    fun addOrRemoveHabitFromSelectedList(habit: Habit) {
        var list = _selectedHabits.value
        if (list == null) {
            list = ArrayList()
        }
        if (list.contains(habit)) {
            list.remove(habit)
        } else {
            list.add(habit)
        }
        _selectedHabits.value = list
    }

    fun isHabitSelected(habit: Habit): Boolean {
        return _selectedHabits.value?.contains(habit) ?: false
    }

    fun clearSelectedHabits() {
        _selectedHabits.value = null
    }
}
