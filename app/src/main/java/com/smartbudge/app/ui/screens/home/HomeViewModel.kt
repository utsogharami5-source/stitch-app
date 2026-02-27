package com.smartbudge.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudge.app.data.local.dao.TransactionDao
import com.smartbudge.app.data.local.dao.UserDao
import com.smartbudge.app.data.local.entity.TransactionEntity
import com.smartbudge.app.data.local.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionDao: TransactionDao,
    private val userDao: UserDao
) : ViewModel() {

    private val currentUserId: String
        get() = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

    val user: StateFlow<UserEntity?> = userDao.getUser(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentTransactions: StateFlow<List<TransactionEntity>> = transactionDao
        .getAllTransactions(currentUserId)
        .map { it.take(10) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalBalance: StateFlow<Double> = transactionDao
        .getAllTransactions(currentUserId)
        .map { list ->
            list.sumOf { if (it.type == "Income") it.amount else -it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val totalIncome: StateFlow<Double> = transactionDao
        .getAllTransactions(currentUserId)
        .map { list ->
            list.filter { it.type == "Income" }.sumOf { it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val totalExpense: StateFlow<Double> = transactionDao
        .getAllTransactions(currentUserId)
        .map { list ->
            list.filter { it.type == "Expense" }.sumOf { it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val monthlySpent: StateFlow<Double> = transactionDao
        .getAllTransactions(currentUserId)
        .map { list ->
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfMonth = calendar.timeInMillis

            list.filter { it.type == "Expense" && it.date >= startOfMonth }
                .sumOf { it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionDao.deleteTransaction(transaction)
        }
    }
}
