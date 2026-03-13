package com.gridibuild.sfobud.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gridibuild.sfobud.data.local.dao.*
import com.gridibuild.sfobud.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        ProjectEntity::class,
        RoomEntity::class,
        TaskEntity::class,
        MaterialEntity::class,
        ShoppingItemEntity::class,
        BudgetExpenseEntity::class,
        MeasurementEntity::class,
        PhotoEntity::class,
        ContactEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun projectDao(): ProjectDao
    abstract fun roomDao(): RoomDao
    abstract fun taskDao(): TaskDao
    abstract fun materialDao(): MaterialDao
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun budgetExpenseDao(): BudgetExpenseDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun photoDao(): PhotoDao
    abstract fun contactDao(): ContactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gridbuild_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
