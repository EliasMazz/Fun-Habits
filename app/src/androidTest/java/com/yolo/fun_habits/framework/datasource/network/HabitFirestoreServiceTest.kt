package com.yolo.fun_habits.framework.datasource.network

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yolo.fun_habits.BaseTest
import com.yolo.fun_habits.business.domain.model.Habit
import com.yolo.fun_habits.di.TestAppComponent
import com.yolo.fun_habits.framework.datasource.data.HabitDataFactory
import com.yolo.fun_habits.framework.datasource.network.abstraction.IHabitFirestoreService
import com.yolo.fun_habits.framework.datasource.network.mapper.HabitNetworkMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

const val PASSWORD = "123456"

@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
class HabitFirestoreServiceTest : BaseTest() {

    private lateinit var habitFirestoreService: IHabitFirestoreService

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var habitDataFactory: HabitDataFactory

    @Inject
    lateinit var habitNetworkMapper: HabitNetworkMapper

    override fun injectTest() {
        (application.appComponent as TestAppComponent).inject(this)
    }

    init {
        injectTest()
        signIn()
        insertTestData()
    }

    @Before
    fun setup() {
        habitFirestoreService = HabitFirestoreServiceImpl(
            firebaseAuth = firebaseAuth,
            firestore = firestore,
            habitNetworkMapper = habitNetworkMapper
        )
    }

    private fun signIn() = runBlocking {
        firebaseAuth.signInWithEmailAndPassword(
            EMAIL,
            PASSWORD
        ).await()
    }

    @Test
    fun insertSingleHabit_ConfirmBySearching() = runBlocking {
        val habit = habitDataFactory.createSingleHabit(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        )

        habitFirestoreService.insertOrUpdateHabit(habit)

        val result = habitFirestoreService.searchHabit(habit)

        assertEquals(habit, result)
    }

    @Test
    fun updateSingleHabit_ConfirmBySearching() = runBlocking {
        val randomHabit = habitFirestoreService.getAllHabits().last()

        val updatedTitle = UUID.randomUUID().toString()
        val updatedBody = UUID.randomUUID().toString()

        val updatedHabit = habitDataFactory.createSingleHabit(
            id = randomHabit.id,
            title = updatedTitle,
            body = updatedBody
        )

        habitFirestoreService.insertOrUpdateHabit(updatedHabit)

        val result = habitFirestoreService.searchHabit(updatedHabit)!!

        assertEquals(updatedTitle, result.title)
        assertEquals(updatedBody, result.body)
    }

    @Test
    fun insertHabitList_ConfirmBySearching() = runBlocking {
        val habitList = habitDataFactory.createHabitList(15)
        habitFirestoreService.insertOrUpdateListHabit(habitList)

        val result = habitFirestoreService.getAllHabits()
        assertTrue { result.containsAll(habitList) }
    }

    @Test
    fun deleteSingleHabit_ConfirmBySearching() = runBlocking {
        val habitToDelete = habitFirestoreService.getAllHabits().first()
        habitFirestoreService.deleteHabit(habitToDelete.id)

        val result = habitFirestoreService.getAllHabits()
        assertFalse { result.contains(habitToDelete) }
    }

    @Test
    fun insertIntoDeletesNode_ConfirmBySearching() = runBlocking {
        val habitToDelete = habitFirestoreService.getAllHabits().last()

        habitFirestoreService.insertDeletedHabit(habitToDelete)

        val result = habitFirestoreService.getDeletedHabitList()
        assertTrue { result.contains(habitToDelete) }
    }

    @Test
    fun insertListIntoDeletesNode() = runBlocking {
        val habitList = ArrayList(habitFirestoreService.getAllHabits())
        //choose some random habits to add to `deletes` node
        val habitsToDelete = mutableListOf<Habit>()
        //1st
        val firstHabitToDelete = habitList.get(Random.nextInt(0, habitList.size - 1) + 1)
        habitList.remove(firstHabitToDelete)
        habitsToDelete.add(firstHabitToDelete)

        //2st
        val secondHabitToDelete = habitList.get(Random.nextInt(0, habitList.size - 1) + 1)
        habitList.remove(secondHabitToDelete)
        habitsToDelete.add(secondHabitToDelete)

        //2st
        val thirdHabitToDelete = habitList.get(Random.nextInt(0, habitList.size - 1) + 1)
        habitList.remove(thirdHabitToDelete)
        habitsToDelete.add(thirdHabitToDelete)

        //insert into deletes node
        habitFirestoreService.insertDeletedHabits(habitsToDelete)

        //confirm they were inserted into deletes node
        val searchResults = habitFirestoreService.getDeletedHabitList()

        assertTrue { searchResults.containsAll(habitsToDelete) }
    }

    @Test
    fun deleteDeleteHabits_ConfirmBySearching() = runBlocking {
        val habit = habitDataFactory.createSingleHabit(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        )

        //insert into deletes node
        habitFirestoreService.insertDeletedHabit(habit)

        //confirm habit is in deletes node
        var result = habitFirestoreService.getDeletedHabitList()
        assertTrue { result.contains(habit) }

        //delete from `deletes` node
        habitFirestoreService.deleteDeletedHabit(habit)

        //confirm habit is not longe in deletes node
        result = habitFirestoreService.getDeletedHabitList()
        assertFalse { result.contains(habit) }
    }

    private fun insertTestData() =
        habitDataFactory.produceListOfHabits()
            .map { habitNetworkMapper.mapToEntity(it) }
            .map {
                firestore.collection(HABITS_COLLECTION)
                    .document(USER_ID)
                    .collection(HABITS_COLLECTION)
                    .document(it.id)
                    .set(it)
            }

}
