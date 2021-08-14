package com.yolo.fun_habits.framework.presentation.habitdetail

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.yolo.fun_habits.BaseTest
import com.yolo.fun_habits.R
import com.yolo.fun_habits.di.TestAppComponent
import com.yolo.fun_habits.framework.datasource.cache.util.HabitCacheMapper
import com.yolo.fun_habits.framework.datasource.data.HabitDataFactory
import com.yolo.fun_habits.framework.presentation.TestHabitFragmentFactory
import com.yolo.fun_habits.framework.presentation.UIController
import com.yolo.fun_habits.framework.util.EspressoIdlingResourceRule
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
class HabitDetailFragmentTest : BaseTest() {

    @get: Rule
    val espressoIdlingResourceRule = EspressoIdlingResourceRule()

    @Inject
    lateinit var fragmentFactory: TestHabitFragmentFactory

    @Inject
    lateinit var cacheMapper: HabitCacheMapper

    @Inject
    lateinit var habitDataFactory: HabitDataFactory

    private val uiController = mockk<UIController>(relaxed = true)

    private val navController = mockk<NavController>(relaxed = true)

    override fun injectTest() {
        (application.appComponent as TestAppComponent).inject(this)
    }

    init {
        injectTest()
    }

    @Before()
    fun setup() {
        setupUiController()
    }

    private fun setupUiController() {
        fragmentFactory.uiController = uiController
    }

    /**
     * It was decided to write a single large test when testing fragments in isolation
     * instead of making multiple tests, because they have issues sharing state. Its possible
     * to solve the issues mentioned by using test orchestrator, but that will prevent me from getting reports
     */
    @Test
    fun generalDetailFragmentTest() {

        val testHabit = habitDataFactory.createSingleHabit(
            id = UUID.randomUUID().toString(),
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString()
        )

        val scenario = launchFragmentInContainer<HabitDetailFragment>(
            factory = fragmentFactory,
            fragmentArgs = bundleOf(HABIT_DETAIL_SELECTED_HABIT_BUNDLE_KEY to testHabit)
        ).onFragment { fragment ->
            fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                if (viewLifecycleOwner != null) {
                    Navigation.setViewNavController(fragment.requireView(), navController)
                }
            }
        }

        //confirm arguments are set from bundle
        onView(withId(R.id.habit_title)).check(matches(withText(testHabit.title)))
        onView(withId(R.id.habit_body)).check(matches(withText(testHabit.body)))

        // press back button
        onView(withId(R.id.toolbar_back_button)).perform(click())

        //confirm NavController attempted to navigate
        verify {
            navController.popBackStack()
        }
    }
}
