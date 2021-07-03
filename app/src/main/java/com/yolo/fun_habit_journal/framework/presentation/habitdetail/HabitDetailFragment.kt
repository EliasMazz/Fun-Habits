package com.yolo.fun_habit_journal.framework.presentation.habitdetail

import android.os.Bundle
import android.view.View
import com.yolo.fun_habit_journal.R
import com.yolo.fun_habit_journal.framework.presentation.common.BaseFragment

const val HABIT_DETAIL_STATE_BUNDLE_KEY = "com.yolo.fun_habit_journal.framework.presentation.habitdetail.state"

class HabitDetailFragment: BaseFragment(R.layout.fragment_habit_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun inject() {
        TODO("Not yet implemented")
    }
}
