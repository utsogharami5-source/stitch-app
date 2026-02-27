package com.smartbudge.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_alerts")
data class BudgetAlertEntity(
    @PrimaryKey(autoGenerate = true) val alert_id: Int = 0,
    val user_id: String,
    val category_id: Int,
    val limit_amount: Double,
    val threshold_percentage: Double
)
