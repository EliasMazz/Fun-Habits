package com.yolo.fun_habits.framework.datasource.network

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yolo.fun_habits.BaseTest
import com.yolo.fun_habits.business.domain.model.HabitFactory
import com.yolo.fun_habits.dependencyinjection.TestAppComponent
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
import kotlin.test.assertEquals

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
    lateinit var habitFactory: HabitFactory

    @Inject
    lateinit var habitNetworkMapper: HabitNetworkMapper

    override fun injectTest() {
        (application.appComponent as TestAppComponent).inject(this)
    }

    init {
        injectTest()
        signIn()
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
    fun insertSingleHabit() = runBlocking {
        val habit = habitFactory.createSingleHabit(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        )

        habitFirestoreService.insertOrUpdateHabit(habit)

        val result = habitFirestoreService.searchHabit(habit)

        assertEquals(habit, result)
    }
}
