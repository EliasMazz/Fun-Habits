package com.codingwithmitch.cleannotes.framework.presentation.end_to_end

import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.google.firebase.auth.FirebaseAuth
import com.yolo.fun_habits.BaseTest
import com.yolo.fun_habits.R
import com.yolo.fun_habits.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habits.di.TestAppComponent
import com.yolo.fun_habits.framework.datasource.cache.model.HabitCacheEntity
import com.yolo.fun_habits.framework.datasource.cache.util.HabitCacheMapper
import com.yolo.fun_habits.framework.datasource.data.HabitDataFactory
import com.yolo.fun_habits.framework.datasource.database.HabitDao
import com.yolo.fun_habits.framework.presentation.MainActivity
import com.yolo.fun_habits.framework.presentation.habitlist.HabitListAdapter
import com.yolo.fun_habits.framework.util.EspressoIdlingResourceRule
import com.yolo.fun_habits.util.PASSWORD
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/*
    --Test cases:
    1. start SplashFragment, confirm logo is visible
    2. Navigate HabitListFragment, confirm list is visible
    3. Select a habit from list, confirm correct title and body is visible
    4. Navigate BACK, confirm HabitListFragment in view
 */
@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
class HabitFeatureTest : BaseTest() {

    //@get: Rule
   // val espressoIdlingResourceRule = EspressoIdlingResourceRule()

    @Inject
    lateinit var cacheMapper: HabitCacheMapper

    @Inject
    lateinit var habitDataFactory: HabitDataFactory

    @Inject
    lateinit var dao: HabitDao

    @Inject
    lateinit var habitNetworkDataSource: IHabitNetworkDataSource

    private val testEntityList: List<HabitCacheEntity>

    init {
        injectTest()
        testEntityList = habitDataFactory.produceListOfHabits().map {
            cacheMapper.mapToEntity(it)
        }
        prepareDataSet(testEntityList)
    }

    // ** Must clear network and cache so there is no previous state issues **
    private fun prepareDataSet(testData: List<HabitCacheEntity>) = runBlocking {
        // clear any existing data so recyclerview isn't overwhelmed
        dao.deleteAllHabits()
        habitNetworkDataSource.deleteAllHabits()
        dao.insertHabitList(testData)
    }

    @Test
    fun generalEndToEndTest() {
        val scenario = launchActivity<MainActivity>()

        //enter password input
        onView(withId(R.id.md_input_message)).perform(typeText(PASSWORD))

        onView(withText(R.string.text_ok)).perform(click())

        // Wait for HabitListFragment to come into view
        waitViewShown(withId(R.id.recycler_view))

        val recyclerView = onView(withId(R.id.recycler_view))

        // confirm HabitListFragment is in view
        recyclerView.check(matches(isDisplayed()))

        // Select a habit from the list
        recyclerView.perform(
            actionOnItemAtPosition<HabitListAdapter.HabitViewHolder>(1, click())
        )

        // Wait for HabitDetailFragment to come into view
        waitViewShown(withId(R.id.habit_body_container))

        // Confirm HabitDetailFragment is in view
        onView(withId(R.id.habit_body_container)).check(matches(isDisplayed()))
        onView(withId(R.id.habit_title)).check(matches(not(withText(""))))
        onView(withId(R.id.habit_body)).check(matches(not(withText(""))))

        // press back arrow in toolbar
        onView(withId(R.id.toolbar_back_button)).perform(click())

        // confirm HabitListFragment is in view
        recyclerView.check(matches(isDisplayed()))

        FirebaseAuth.getInstance().signOut()
    }

    override fun injectTest() {
        (application.appComponent as TestAppComponent).inject(this)
    }
}












