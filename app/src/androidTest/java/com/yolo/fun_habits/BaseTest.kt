package com.yolo.fun_habits

import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.yolo.fun_habits.framework.presentation.TestBaseApplication
import com.yolo.fun_habits.framework.util.ViewShownIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.hamcrest.Matcher

abstract class BaseTest {

    @ExperimentalCoroutinesApi
    @FlowPreview
    val application: TestBaseApplication = ApplicationProvider.getApplicationContext() as TestBaseApplication

    abstract fun injectTest()

    // wait for a certain view to be shown.
    fun waitViewShown(matcher: Matcher<View>) {
        val idlingResource: IdlingResource = ViewShownIdlingResource(matcher, isDisplayed())
        try {
            IdlingRegistry.getInstance().register(idlingResource)
            onView(withId(0)).check(doesNotExist())
        } finally {
            IdlingRegistry.getInstance().unregister(idlingResource)
        }
    }
}
