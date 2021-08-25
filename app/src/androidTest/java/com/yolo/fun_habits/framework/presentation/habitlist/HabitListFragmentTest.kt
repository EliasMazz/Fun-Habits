package com.yolo.fun_habits.framework.presentation.habitlist

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.yolo.fun_habits.BaseTest
import com.yolo.fun_habits.R
import com.yolo.fun_habits.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habits.di.TestAppComponent
import com.yolo.fun_habits.framework.datasource.cache.model.HabitCacheEntity
import com.yolo.fun_habits.framework.datasource.cache.util.HabitCacheMapper
import com.yolo.fun_habits.framework.datasource.data.HabitDataFactory
import com.yolo.fun_habits.framework.datasource.database.HabitDao
import com.yolo.fun_habits.framework.presentation.TestHabitFragmentFactory
import com.yolo.fun_habits.framework.presentation.UIController
import com.yolo.fun_habits.framework.presentation.habitdetail.HABIT_DETAIL_SELECTED_HABIT_BUNDLE_KEY
import com.yolo.fun_habits.framework.presentation.habitdetail.HabitDetailFragment
import com.yolo.fun_habits.framework.util.EspressoIdlingResourceRule
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
class HabitListFragmentTest : BaseTest() {

    //@get:Rule
    //val espressoIdlingResourceRule = EspressoIdlingResourceRule()

    @Inject
    lateinit var fragmentFactory: TestHabitFragmentFactory

    @Inject
    lateinit var cacheMapper: HabitCacheMapper

    @Inject
    lateinit var habitDataFactory: HabitDataFactory


    @Inject
    lateinit var dao: HabitDao

    private val testEntityList: List<HabitCacheEntity>

    private val uiController = mockk<UIController>(relaxed = true)

    private val navController = mockk<NavController>(relaxed = true)

    override fun injectTest() {
        (application.appComponent as TestAppComponent).inject(this)
    }


    init {
        injectTest()

        testEntityList = habitDataFactory.produceListOfHabits().map {
            cacheMapper.mapToEntity(it)
        }
        prepareDataSet(testEntityList)
    }

    @Before
    fun setup() {
        setupUiController()
    }

    private fun setupUiController() {
        fragmentFactory.uiController = uiController
    }

    private fun prepareDataSet(testData: List<HabitCacheEntity>) = runBlocking {
        // clear any existing data so recyclerview isn't overwhelmed
        dao.deleteAllHabits()
        dao.insertHabitList(testData)
    }


    @Test
    fun generalListHabitFragment(){

        val scenario = launchFragmentInContainer<HabitListFragment>(
            factory = fragmentFactory
        ).onFragment { fragment ->
            fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                if (viewLifecycleOwner != null) {
                    Navigation.setViewNavController(fragment.requireView(), navController)
                }
            }
        }

        // Wait for HabitListFragment to come into view
        waitViewShown(ViewMatchers.withId(R.id.recycler_view))

        val recyclerView = Espresso.onView(ViewMatchers.withId(R.id.recycler_view))

        // confirm recyclerview is in view
        recyclerView.check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        for(entity in testEntityList){
            recyclerView.perform(
                RecyclerViewActions.scrollTo<HabitListAdapter.HabitViewHolder>(ViewMatchers.hasDescendant(withText(entity.title)))
            )
            Espresso.onView(withText(entity.title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }

    }
}
