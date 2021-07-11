package com.yolo.fun_habit_journal.dependencyinjection

import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yolo.fun_habit_journal.business.data.cache.HabitCacheDataSourceImpl
import com.yolo.fun_habit_journal.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habit_journal.business.data.network.HabitNetworkDataSourceImpl
import com.yolo.fun_habit_journal.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habit_journal.business.domain.model.HabitFactory
import com.yolo.fun_habit_journal.business.domain.util.DateUtil
import com.yolo.fun_habit_journal.business.usecases.appstart.usecase.SyncDeletedHabitsUseCase
import com.yolo.fun_habit_journal.business.usecases.appstart.usecase.SyncHabitsUseCase
import com.yolo.fun_habit_journal.business.usecases.common.usecase.DeleteHabitUseCase
import com.yolo.fun_habit_journal.business.usecases.habitdetail.HabitDetailInteractors
import com.yolo.fun_habit_journal.business.usecases.habitdetail.usecase.UpdateHabitUseCase
import com.yolo.fun_habit_journal.business.usecases.habitlist.HabitListInteractors
import com.yolo.fun_habit_journal.business.usecases.habitlist.usecase.DeleteMultipleHabitsUseCase
import com.yolo.fun_habit_journal.business.usecases.habitlist.usecase.GetHabitsCountUseCase
import com.yolo.fun_habit_journal.business.usecases.habitlist.usecase.InsertNewHabitUseCase
import com.yolo.fun_habit_journal.business.usecases.habitlist.usecase.RestoreDeletedHabitUseCase
import com.yolo.fun_habit_journal.business.usecases.habitlist.usecase.SearchHabitsUseCase
import com.yolo.fun_habit_journal.framework.datasource.cache.HabitDaoServiceImpl
import com.yolo.fun_habit_journal.framework.datasource.cache.abstraction.IHabitDaoService
import com.yolo.fun_habit_journal.framework.datasource.cache.util.HabitCacheMapper
import com.yolo.fun_habit_journal.framework.datasource.database.HabitDao
import com.yolo.fun_habit_journal.framework.datasource.database.HabitDatabase
import com.yolo.fun_habit_journal.framework.datasource.network.HabitFirestoreServiceImpl
import com.yolo.fun_habit_journal.framework.datasource.network.abstraction.IHabitFirestoreService
import com.yolo.fun_habit_journal.framework.datasource.network.mapper.HabitNetworkMapper
import com.yolo.fun_habit_journal.framework.presentation.habitdetail.state.HabitDetailViewState
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListViewState
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@FlowPreview
@Module
object AppModule {


    // https://developer.android.com/reference/java/text/SimpleDateFormat.html?hl=pt-br
    @JvmStatic
    @Singleton
    @Provides
    fun provideDateFormat(): SimpleDateFormat {
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
        sdf.timeZone = TimeZone.getTimeZone("UTC-7") // match firestore
        return sdf
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideDateUtil(dateFormat: SimpleDateFormat): DateUtil {
        return DateUtil(
            dateFormat
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSharedPrefsEditor(
        sharedPreferences: SharedPreferences
    ): SharedPreferences.Editor {
        return sharedPreferences.edit()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideHabitFactory(dateUtil: DateUtil): HabitFactory {
        return HabitFactory(dateUtil)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideHabitDAO(habitDatabase: HabitDatabase): HabitDao {
        return habitDatabase.habitDao()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideHabitCacheMapper(dateUtil: DateUtil): HabitCacheMapper {
        return HabitCacheMapper(dateUtil)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideHabitNetworkMapper(dateUtil: DateUtil): HabitNetworkMapper {
        return HabitNetworkMapper(dateUtil)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideHabitDaoService(
        noteDao: HabitDao,
        habitCacheMapper: HabitCacheMapper,
        dateUtil: DateUtil
    ): IHabitDaoService {
        return HabitDaoServiceImpl(noteDao, habitCacheMapper, dateUtil)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideHabitCacheDataSource(
        habitDaoService: IHabitDaoService
    ): IHabitCacheDataSource {
        return HabitCacheDataSourceImpl(habitDaoService)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirestoreService(
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore,
        networkMapper: HabitNetworkMapper
    ): IHabitFirestoreService {
        return HabitFirestoreServiceImpl(
            firebaseAuth,
            firebaseFirestore,
            networkMapper
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideHabitNetworkDataSource(
        firestoreService: HabitFirestoreServiceImpl
    ): HabitNetworkDataSourceImpl {
        return HabitNetworkDataSourceImpl(
            firestoreService
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSyncHabits(
        habitCacheDataSource: IHabitCacheDataSource,
        habitNetworkDataSource: IHabitNetworkDataSource
    ): SyncHabitsUseCase {
        return SyncHabitsUseCase(
            habitCacheDataSource,
            habitNetworkDataSource
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSyncDeletedHabits(
        habitCacheDataSource: IHabitCacheDataSource,
        habitNetworkDataSource: IHabitNetworkDataSource
    ): SyncDeletedHabitsUseCase {
        return SyncDeletedHabitsUseCase(
            habitCacheDataSource,
            habitNetworkDataSource
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideHabitDetailInteractors(
        habitCacheDataSource: IHabitCacheDataSource,
        habitNetworkDataSource: IHabitNetworkDataSource
    ): HabitDetailInteractors {
        return HabitDetailInteractors(
            DeleteHabitUseCase<HabitDetailViewState>(habitCacheDataSource, habitNetworkDataSource),
            UpdateHabitUseCase(habitCacheDataSource, habitNetworkDataSource)
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideHabitListInteractors(
        habitCacheDataSource: IHabitCacheDataSource,
        habitNetworkDataSource: IHabitNetworkDataSource,
        habitFactory: HabitFactory
    ): HabitListInteractors {
        return HabitListInteractors(
            InsertNewHabitUseCase(habitCacheDataSource, habitNetworkDataSource, habitFactory),
            DeleteHabitUseCase<HabitListViewState>(habitCacheDataSource, habitNetworkDataSource),
            SearchHabitsUseCase(habitCacheDataSource),
            GetHabitsCountUseCase(habitCacheDataSource),
            RestoreDeletedHabitUseCase(habitCacheDataSource, habitNetworkDataSource),
            DeleteMultipleHabitsUseCase(habitCacheDataSource, habitNetworkDataSource)
        )
    }
}
