package com.yolo.fun_habit_journal.dependencyinjection

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.yolo.fun_habit_journal.framework.datasource.database.HabitDatabase
import com.yolo.fun_habit_journal.framework.presentation.BaseApplication
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@FlowPreview
@Module
object ProductionModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideHabitDb(app: BaseApplication): HabitDatabase {
        return Room.databaseBuilder(app, HabitDatabase::class.java, HabitDatabase.DATABASE_NAME)
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
