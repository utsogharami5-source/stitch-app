package com.smartbudge.app.ui.screens.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudge.app.data.local.dao.TransactionDao
import com.smartbudge.app.data.local.entity.TransactionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionDetailsViewModel @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: com.smartbudge.app.data.local.dao.CategoryDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val transactionId: Int = savedStateHandle.get<Int>("transactionId") ?: -1

    val transaction: StateFlow<TransactionEntity?> = transactionDao.getTransactionById(transactionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val categoryName: StateFlow<String> = transaction.flatMapLatest { trans ->
        if (trans == null) flowOf("Uncategorized")
        else categoryDao.getCategoryById(trans.category_id).map { it?.name ?: "Uncategorized" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Uncategorized")

    fun deleteTransaction(transaction: TransactionEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            transactionDao.deleteTransaction(transaction)
            onSuccess()
        }
    }
}
