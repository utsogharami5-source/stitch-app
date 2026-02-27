package com.smartbudge.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val user_id: String,
    val name: String,
    val email: String,
    val monthly_budget: Double,
    val created_at: Long
)
