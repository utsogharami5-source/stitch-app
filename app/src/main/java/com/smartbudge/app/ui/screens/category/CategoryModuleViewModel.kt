package com.smartbudge.app.ui.screens.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudge.app.data.local.dao.TransactionDao
import com.smartbudge.app.data.local.entity.TransactionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoryModuleViewModel @Inject constructor(
    private val transactionDao: TransactionDao
) : ViewModel() {

    private val currentUserId: String
        get() = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

    private val _categoryName = MutableStateFlow("")
    val categoryName: StateFlow<String> = _categoryName.asStateFlow()

    val transactions: StateFlow<List<TransactionEntity>> = _categoryName
        .flatMapLatest { name ->
            if (name.isEmpty()) flowOf(emptyList())
            else {
                val namesToMatch = when (name.lowercase()) {
                    "food" -> listOf("Food & Drinks", "Groceries", "Dining Out")
                    "entertainment" -> listOf("Entertainment")
                    "utilities" -> listOf("Utilities", "Internet & Phone")
                    else -> listOf(name)
                }
                
                transactionDao.getTransactionsByCategoryNames(currentUserId, namesToMatch)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCategory(name: String) {
        _categoryName.value = name
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionDao.deleteTransaction(transaction)
        }
    }
}
