package com.yolo.fun_habits.di

import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.yolo.fun_habits.business.domain.model.HabitFactory
import com.yolo.fun_habits.framework.datasource.data.HabitDataFactory
import com.yolo.fun_habits.framework.datasource.database.HabitDatabase
import com.yolo.fun_habits.framework.presentation.TestBaseApplication
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@FlowPreview
@Module
object TestModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideHabitDb(app: TestBaseApplication): HabitDatabase {
        return Room.inMemoryDatabaseBuilder(app, HabitDatabase::class.java)
            .fallbackToDestructiveMigration()
            .build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirestoreSettings(): FirebaseFirestoreSettings {
        return FirebaseFirestoreSettings.Builder()
            .setHost("10.0.2.2:8080")
            .setSslEnabled(false)
            .setPersistenceEnabled(false)
            .build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirebaseFirestore(settings: FirebaseFirestoreSettings): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = settings
        return firestore
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideHabitDataFactory(
        application: TestBaseApplication,
        habitFactory: HabitFactory
    ): HabitDataFactory = HabitDataFactory(application, habitFactory)


}
