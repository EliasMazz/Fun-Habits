package com.yolo.fun_habit_journal.framework.presentation.habitdetail.state

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class HabitInteractionManager{

    private val _habitTitleState: MutableLiveData<HabitInteractionState>
            = MutableLiveData(HabitInteractionState.DefaultState())

    private val _habitBodyState: MutableLiveData<HabitInteractionState>
            = MutableLiveData(HabitInteractionState.DefaultState())

    private val _collapsingToolbarState: MutableLiveData<CollapsingToolbarState>
            = MutableLiveData(CollapsingToolbarState.Expanded())

    val habitTitleState: LiveData<HabitInteractionState>
            get() = _habitTitleState

    val noteBodyState: LiveData<HabitInteractionState>
        get() = _habitBodyState

    val collapsingToolbarState: LiveData<CollapsingToolbarState>
        get() = _collapsingToolbarState

    fun setCollapsingToolbarState(state: CollapsingToolbarState){
        if(!state.toString().equals(_collapsingToolbarState.value.toString())){
            _collapsingToolbarState.value = state
        }
    }

    fun setNewNoteTitleState(state: HabitInteractionState){
        if(habitTitleState.toString() != state.toString()){
            _habitTitleState.value = state
            when(state){

                is HabitInteractionState.EditState -> {
                    _habitBodyState.value = HabitInteractionState.DefaultState()
                }
            }
        }
    }

    fun setNewNoteBodyState(state: HabitInteractionState){
        if(noteBodyState.toString() != state.toString()){
            _habitBodyState.value = state
            when(state){
                is HabitInteractionState.EditState -> {
                    _habitTitleState.value = HabitInteractionState.DefaultState()
                }
            }
        }
    }

    fun isEditingTitle() = habitTitleState.value.toString().equals(HabitInteractionState.EditState().toString())

    fun isEditingBody() = noteBodyState.value.toString().equals(HabitInteractionState.EditState().toString())

    fun exitEditState(){
        _habitTitleState.value = HabitInteractionState.DefaultState()
        _habitBodyState.value = HabitInteractionState.DefaultState()
    }

    // return true if either title or body are in EditState
    fun checkEditState() = habitTitleState.value.toString().equals(HabitInteractionState.EditState().toString())
            || noteBodyState.value.toString().equals(HabitInteractionState.EditState().toString())



}

















