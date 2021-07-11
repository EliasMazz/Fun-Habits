package com.yolo.fun_habit_journal.framework.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yolo.fun_habit_journal.R
import com.yolo.fun_habit_journal.business.domain.state.DialogInputCaptureCallback
import com.yolo.fun_habit_journal.business.domain.state.Response
import com.yolo.fun_habit_journal.business.domain.state.StateMessageCallback
import com.yolo.fun_habit_journal.framework.presentation.common.HabitFragmentFactory
import com.yolo.fun_habit_journal.framework.util.printLogD
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class MainActivity : AppCompatActivity(), UIController {
    private val TAG: String = "AppDebug"

    @Inject
    lateinit var fragmentFactory: HabitFragmentFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        setFragmentFactory()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    private fun setFragmentFactory() {
        supportFragmentManager.fragmentFactory = fragmentFactory
    }

    private fun inject() {
        (application as BaseApplication).appComponent.inject(this)
    }

    override fun displayProgressBar(isDisplayed: Boolean) {
        // TODO("Not yet implemented")
    }

    override fun hideSoftKeyboard() {
        // TODO("Not yet implemented")
    }

    override fun displayInputCaptureDialog(title: String, callback: DialogInputCaptureCallback) {
        // TODO("Not yet implemented")
    }

    override fun onResponseReceived(
        response: Response,
        stateMessageCallback: StateMessageCallback
    ) {
        // TODO("Not yet implemented")
        printLogD("MainActivity", "response: ${response}")
    }

}
