package com.yolo.fun_habits.framework.util

import androidx.test.espresso.IdlingRegistry
import com.yolo.fun_habits.util.EspressoIdlingResource
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class EspressoIdlingResourceRule : TestWatcher(){

    private val  className = "EspressoIdlingResourceRule"

    private val idlingResource = EspressoIdlingResource.countingIdlingResource

    override fun finished(description: Description?) {
        printLogD(className, "FINISHED")
        IdlingRegistry.getInstance().unregister(idlingResource)
        super.finished(description)
    }

    override fun starting(description: Description?) {
        printLogD(className, "STARTING")
        IdlingRegistry.getInstance().register(idlingResource)
        super.starting(description)
    }
}
