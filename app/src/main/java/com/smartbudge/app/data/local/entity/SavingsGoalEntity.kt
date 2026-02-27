package com.smartbudge.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val goal_id: Int = 0,
    val user_id: String,
    val title: String,
    val target_amount: Double,
    val saved_amount: Double,
    val deadline: Long
)
