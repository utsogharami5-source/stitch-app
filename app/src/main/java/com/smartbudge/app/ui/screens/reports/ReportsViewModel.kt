package com.smartbudge.app.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudge.app.data.local.dao.CategoryDao
import com.smartbudge.app.data.local.dao.TransactionDao
import com.smartbudge.app.data.local.entity.CategoryEntity
import com.smartbudge.app.data.local.entity.TransactionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class CategorySpending(
    val category: CategoryEntity,
    val totalAmount: Double,
    val percentage: Float
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) : ViewModel() {

    private val userId: String
        get() = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

    private val _timeFilter = MutableStateFlow("Monthly") // Weekly, Monthly, Yearly
    val timeFilter: StateFlow<String> = _timeFilter.asStateFlow()

    val categorySpending: StateFlow<List<CategorySpending>> = combine(
        transactionDao.getAllTransactions(userId),
        categoryDao.getAllCategories(userId),
        _timeFilter
    ) { transactions, categories, filter ->
        val calendar = java.util.Calendar.getInstance()
        val endTime = calendar.timeInMillis
        
        val startTime = when (filter) {
            "Weekly" -> {
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -7)
                calendar.timeInMillis
            }
            "Yearly" -> {
                calendar.set(java.util.Calendar.DAY_OF_YEAR, 1)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.timeInMillis
            }
            else -> { // Monthly
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.timeInMillis
            }
        }

        val expenses = transactions.filter { it.type == "Expense" && it.date in startTime..endTime }
        val totalExpense = expenses.sumOf { it.amount }

        if (totalExpense == 0.0) return@combine emptyList<CategorySpending>()

        categories.map { category ->
            val amount = expenses.filter { it.category_id == category.category_id }.sumOf { it.amount }
            CategorySpending(
                category = category,
                totalAmount = amount,
                percentage = (amount / totalExpense).toFloat()
            )
        }.filter { it.totalAmount > 0 }.sortedByDescending { it.totalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTimeFilter(filter: String) {
        _timeFilter.value = filter
    }
}
