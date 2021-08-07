package com.yolo.fun_habits.dependencyinjection

import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
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
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}
