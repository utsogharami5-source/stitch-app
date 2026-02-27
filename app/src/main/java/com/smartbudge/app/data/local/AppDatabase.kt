package com.smartbudge.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smartbudge.app.data.local.dao.*
import com.smartbudge.app.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        TransactionEntity::class,
        CategoryEntity::class,
        SavingsGoalEntity::class,
        BudgetAlertEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun budgetAlertDao(): BudgetAlertDao
}
