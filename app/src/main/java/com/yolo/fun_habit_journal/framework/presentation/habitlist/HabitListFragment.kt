package com.yolo.fun_habit_journal.framework.presentation.habitlist

import android.os.Bundle
import android.view.View
import com.yolo.fun_habit_journal.R
import com.yolo.fun_habit_journal.framework.presentation.common.BaseFragment

const val HABIT_LIST_STATE_BUNDLE_KEY = "com.yolo.fun_habit_journal.framework.presentation.habitlist.state"

class HabitListFragment : BaseFragment(R.layout.fragment_habit_list) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun inject() {
        TODO("Not yet implemented")
    }
}
