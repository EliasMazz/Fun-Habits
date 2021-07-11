package com.yolo.fun_habit_journal.framework.datasource.network

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.framework.datasource.network.abstraction.IHabitFirestoreService
import com.yolo.fun_habit_journal.framework.datasource.network.mapper.HabitNetworkMapper
import com.yolo.fun_habit_journal.framework.datasource.network.model.HabitNetworkEntity
import com.yolo.fun_habit_journal.framework.util.crashliticsLogs
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

const val HABITS_COLLECTION = "habits"
const val USERS_COLLECTION = "users"
const val HABITS_DELETED_COLLECTION = "habits_deleted"
const val USER_ID = "dJFxDcMBmzL80FJNyYLAjcFjBnL2" //TODO hardcoded to test
const val EMAIL = "test@test.com"

@Singleton
class HabitFirestoreServiceImpl
@Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val habitNetworkMapper: HabitNetworkMapper
) : IHabitFirestoreService {
    override suspend fun insertOrUpdateHabit(habit: Habit) {
        val entity = habitNetworkMapper.mapToEntity(habit)
        entity.updated_at = Timestamp.now()
        firestore.collection(HABITS_COLLECTION)
            .document(USER_ID)
            .collection(HABITS_COLLECTION)
            .document(entity.id)
            .set(entity)
            .addOnFailureListener { "insertOrUpdateHabit" + crashliticsLogs(it.message) }
            .await()
    }

    override suspend fun insertOrUpdateListHabit(listHabit: List<Habit>) {
        if (listHabit.size > 500) {
            throw Exception("Cant insert more than 500 habits at a time into firestore")
        }

        val collectionRef = firestore
            .collection(HABITS_COLLECTION)
            .document(USER_ID)
            .collection(HABITS_COLLECTION)

        firestore
            .runBatch { batch ->
                for (habit in listHabit) {
                    val entity = habitNetworkMapper.mapToEntity(habit)
                    entity.updated_at = Timestamp.now()
                    val documentRef = collectionRef.document(habit.id)
                    batch.set(documentRef, entity)
                }
            }.addOnFailureListener { "insertOrUpdateListHabit" + crashliticsLogs(it.message) }
            .await()
    }

    override suspend fun deleteHabit(id: String) {
        firestore
            .collection(HABITS_COLLECTION)
            .document(USER_ID)
            .collection(HABITS_COLLECTION)
            .document(id)
            .delete()
            .addOnFailureListener { "deleteHabit" + crashliticsLogs(it.message) }
            .await()
    }

    override suspend fun insertDeletedHabit(habit: Habit) {
        val entity = habitNetworkMapper.mapToEntity(habit)
        firestore.collection(HABITS_DELETED_COLLECTION)
            .document(USER_ID)
            .collection(HABITS_DELETED_COLLECTION)
            .document(entity.id)
            .set(entity)
            .addOnFailureListener { "insertDeletedHabit" + crashliticsLogs(it.message) }
            .await()
    }

    override suspend fun insertDeletedHabits(habitList: List<Habit>) {
        if (habitList.size > 500) {
            throw Exception("Cant insert more than 500 habits at a time into firestore")
        }

        val collectionRef = firestore
            .collection(HABITS_DELETED_COLLECTION)
            .document(USER_ID)
            .collection(HABITS_DELETED_COLLECTION)

        firestore.runBatch { batch ->
            for (habit in habitList) {
                val entity = habitNetworkMapper.mapToEntity(habit)
                val documentRef = collectionRef.document(habit.id)
                batch.set(documentRef, entity)
            }
        }.addOnFailureListener { "insertDeletedHabits" + crashliticsLogs(it.message) }
            .await()
    }

    override suspend fun deleteDeletedHabit(habit: Habit) {
        firestore.collection(HABITS_DELETED_COLLECTION)
            .document(USER_ID)
            .collection(HABITS_DELETED_COLLECTION)
            .document(habit.id)
            .delete()
            .addOnFailureListener { "deleteDeletedHabit" + crashliticsLogs(it.message) }
            .await()
    }

    override suspend fun deleteAllHabits() {
        firestore.collection(HABITS_DELETED_COLLECTION)
            .document(USER_ID)
            .delete()
            .addOnFailureListener { "deleteAllHabits" + HABITS_DELETED_COLLECTION + crashliticsLogs(it.message) }
            .await()

        firestore.collection(HABITS_COLLECTION)
            .document(USER_ID)
            .delete()
            .addOnFailureListener { "deleteAllHabits" + HABITS_COLLECTION + crashliticsLogs(it.message) }
            .await()
    }

    override suspend fun getDeletedHabitList(): List<Habit> =
        firestore
            .collection(HABITS_DELETED_COLLECTION)
            .document(USER_ID)
            .collection(HABITS_DELETED_COLLECTION)
            .get()
            .addOnFailureListener { "getDeletedHabitList" + crashliticsLogs(it.message) }
            .await().toObjects(HabitNetworkEntity::class.java)
            .map { habitNetworkMapper.mapFromEntity(it) }

    override suspend fun searchHabit(habit: Habit): Habit? =
        firestore
            .collection(HABITS_COLLECTION)
            .document(USER_ID)
            .collection(HABITS_COLLECTION)
            .document(habit.id)
            .get()
            .addOnFailureListener { "searchHabit" + crashliticsLogs(it.message) }
            .await()
            .toObject(HabitNetworkEntity::class.java)?.let {
                habitNetworkMapper.mapFromEntity(it)
            }

    override suspend fun getAllHabits(): List<Habit> =
        firestore
            .collection(HABITS_COLLECTION)
            .document(USER_ID)
            .collection(HABITS_COLLECTION)
            .get()
            .addOnFailureListener { "getAllHabits" + crashliticsLogs(it.message) }
            .await().toObjects(HabitNetworkEntity::class.java)
            .map { habitNetworkMapper.mapFromEntity(it) }
}
