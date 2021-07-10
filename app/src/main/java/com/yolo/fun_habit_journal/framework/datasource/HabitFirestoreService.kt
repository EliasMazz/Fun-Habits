package com.yolo.fun_habit_journal.framework.datasource

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.framework.datasource.network.abstraction.IHabitFirestoreService
import com.yolo.fun_habit_journal.framework.datasource.network.mapper.NetworkMapper
import com.yolo.fun_habit_journal.framework.datasource.network.model.HabitNetworkEntity
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

const val HABITS_COLLECTION = "habits"
const val USERS_COLLECTION = "users"
const val HABITS_DELETED_COLLECTION = "habits_deleted"
const val USER_ID = "dJFxDcMBmzL80FJNyYLAjcFjBnL2" //TODO hardcoded to test
const val EMAIL = "test@test.com"

@Singleton
class HabitFirestoreService
@Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val networkMapper: NetworkMapper
) : IHabitFirestoreService {
    override suspend fun insertOrUpdateHabit(habit: Habit) {
        val entity = networkMapper.mapToEntity(habit)
        entity.updated_at = Timestamp.now()
        firestore.collection(HABITS_COLLECTION)
            .document(USER_ID)
            .collection(HABITS_COLLECTION)
            .document(entity.id)
            .set(entity)
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

        firestore.runBatch { batch ->
            for (habit in listHabit) {
                val entity = networkMapper.mapToEntity(habit)
                entity.updated_at = Timestamp.now()
                val documentRef = collectionRef.document(habit.id)
                batch.set(documentRef, entity)
            }
        }
    }

    override suspend fun deleteHabit(id: String) {
        firestore
            .collection(HABITS_COLLECTION)
            .document(USER_ID)
            .collection(HABITS_COLLECTION)
            .document(id)
            .delete()
            .await()
    }

    override suspend fun insertDeletedHabit(habit: Habit) {
        val entity = networkMapper.mapToEntity(habit)
        firestore.collection(HABITS_DELETED_COLLECTION)
            .document(USER_ID)
            .collection(HABITS_DELETED_COLLECTION)
            .document(entity.id)
            .set(entity)
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
                val entity = networkMapper.mapToEntity(habit)
                val documentRef = collectionRef.document(habit.id)
                batch.set(documentRef, entity)
            }
        }
    }

    override suspend fun deleteDeletedHabit(habit: Habit) {
        firestore.collection(HABITS_DELETED_COLLECTION)
            .document(USER_ID)
            .collection(HABITS_DELETED_COLLECTION)
            .document(habit.id)
            .delete()
            .await()
    }

    override suspend fun deleteAllHabits() {
        firestore.collection(HABITS_DELETED_COLLECTION)
            .document(USER_ID)
            .delete()
            .await()

        firestore.collection(HABITS_COLLECTION)
            .document(USER_ID)
            .delete()
            .await()
    }

    override suspend fun getDeletedHabitList(): List<Habit> =
        firestore
            .collection(HABITS_DELETED_COLLECTION)
            .document(USER_ID)
            .collection(HABITS_DELETED_COLLECTION)
            .get()
            .await().toObjects(HabitNetworkEntity::class.java)
            .map { networkMapper.mapFromEntity(it) }

    override suspend fun searchHabit(habit: Habit): Habit? =
        firestore
            .collection(HABITS_COLLECTION)
            .document(USER_ID)
            .collection(HABITS_COLLECTION)
            .document(habit.id)
            .get()
            .await()
            .toObject(HabitNetworkEntity::class.java)?.let {
                networkMapper.mapFromEntity(it)
            }

    override suspend fun getAllHabits(): List<Habit> =
        firestore
            .collection(HABITS_COLLECTION)
            .document(USER_ID)
            .collection(HABITS_COLLECTION)
            .get()
            .await().toObjects(HabitNetworkEntity::class.java)
            .map { networkMapper.mapFromEntity(it) }
}
