package com.smartbudge.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smartbudge.app.data.local.entity.BudgetAlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetAlertDao {
    @Query("SELECT * FROM budget_alerts WHERE user_id = :userId")
    fun getBudgetAlerts(userId: String): Flow<List<BudgetAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: BudgetAlertEntity)

    @Update
    suspend fun updateAlert(alert: BudgetAlertEntity)

    @Delete
    suspend fun deleteAlert(alert: BudgetAlertEntity)
}
