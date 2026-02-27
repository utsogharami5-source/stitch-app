package com.smartbudge.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val transaction_id: Int = 0,
    val user_id: String,
    val type: String, // Income or Expense
    val amount: Double,
    val category_id: Int,
    val date: Long,
    val note: String,
    val receipt_url: String? = null
)
