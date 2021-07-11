package com.yolo.fun_habit_journal.framework.presentation.habitdetail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.yolo.fun_habit_journal.R
import com.yolo.fun_habit_journal.framework.presentation.common.BaseFragment
import com.yolo.fun_habit_journal.framework.presentation.common.HabitViewModelFactory
import com.yolo.fun_habit_journal.framework.presentation.habitlist.HabitListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

const val HABIT_DETAIL_STATE_BUNDLE_KEY = "com.yolo.fun_habit_journal.framework.presentation.habitdetail.state"
const val HABIT_DETAIL_SELECTED_HABIT_BUNDLE_KEY = "selectedHabit"
const val HABIT_TITLE_CANNOT_BE_EMPTY = "Habit title can not be empty."

@ExperimentalCoroutinesApi
@FlowPreview
class HabitDetailFragment constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : BaseFragment(R.layout.fragment_habit_detail) {

    val viewModel: HabitDetailViewModel by viewModels {
        viewModelFactory
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun inject() {
        TODO("Not yet implemented")
    }
}
