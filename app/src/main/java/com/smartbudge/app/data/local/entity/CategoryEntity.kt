package com.smartbudge.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val category_id: Int = 0,
    val user_id: String?,
    val name: String,
    val type: String, // Income or Expense
    val color: String
)
