package com.smartbudge.app.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudge.app.data.local.dao.CategoryDao
import com.smartbudge.app.data.local.dao.TransactionDao
import com.smartbudge.app.data.local.entity.CategoryEntity
import com.smartbudge.app.data.local.entity.TransactionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) : ViewModel() {

    private val currentUserId: String
        get() = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    private val _isIncome = MutableStateFlow(false)
    val isIncome: StateFlow<Boolean> = _isIncome.asStateFlow()

    private val _selectedCategory = MutableStateFlow<CategoryEntity?>(null)
    val selectedCategory: StateFlow<CategoryEntity?> = _selectedCategory.asStateFlow()

    private val _newCategoryName = MutableStateFlow("")
    val newCategoryName: StateFlow<String> = _newCategoryName.asStateFlow()

    private val _editingTransactionId = MutableStateFlow<Int?>(null)
    val editingTransactionId: StateFlow<Int?> = _editingTransactionId.asStateFlow()

    val categories: StateFlow<List<CategoryEntity>> = _isIncome
        .flatMapLatest { isInc ->
            val type = if (isInc) "Income" else "Expense"
            categoryDao.getAllCategories(currentUserId).map { list ->
                list.filter { it.type == type }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setAmount(value: String) {
        _amount.value = value
    }

    fun setNote(value: String) {
        _note.value = value
    }

    fun setNewCategoryName(value: String) {
        _newCategoryName.value = value
    }

    fun addNewCategory() {
        val name = _newCategoryName.value.trim()
        if (name.isEmpty()) return
        
        viewModelScope.launch {
            val type = if (_isIncome.value) "Income" else "Expense"
            val newCategory = CategoryEntity(
                name = name,
                type = type,
                color = "#34C759",
                user_id = currentUserId
            )
            categoryDao.insertCategory(newCategory)
            _newCategoryName.value = ""
        }
    }

    fun setIsIncome(value: Boolean) {
        _isIncome.value = value
        _selectedCategory.value = null // Reset category when type changes
    }

    fun setCategory(category: CategoryEntity) {
        _selectedCategory.value = category
    }

    fun saveTransaction(onSuccess: () -> Unit) {
        val parsedAmount = _amount.value.toDoubleOrNull() ?: return
        val type = if (_isIncome.value) "Income" else "Expense"
        val transaction = TransactionEntity(
            transaction_id = _editingTransactionId.value ?: 0,
            user_id = currentUserId,
            type = type,
            amount = parsedAmount,
            category_id = _selectedCategory.value?.category_id ?: 0,
            date = System.currentTimeMillis(),
            note = _note.value
        )
        viewModelScope.launch {
            if (_editingTransactionId.value != null) {
                transactionDao.updateTransaction(transaction)
            } else {
                transactionDao.insertTransaction(transaction)
            }
            onSuccess()
        }
    }

    init {
        // Pre-populate some categories if none exist (for demo purposes)
        viewModelScope.launch {
            categoryDao.getAllCategories(currentUserId).first().let {
                if (it.isEmpty()) {
                    val defaultCategories = listOf(
                        // Income Categories (6)
                        CategoryEntity(name = "Salary", type = "Income", color = "#34C759", user_id = currentUserId),
                        CategoryEntity(name = "Freelance", type = "Income", color = "#34C759", user_id = currentUserId),
                        CategoryEntity(name = "Investments", type = "Income", color = "#34C759", user_id = currentUserId),
                        CategoryEntity(name = "Gifts", type = "Income", color = "#34C759", user_id = currentUserId),
                        CategoryEntity(name = "Rental", type = "Income", color = "#34C759", user_id = currentUserId),
                        CategoryEntity(name = "Other Income", type = "Income", color = "#34C759", user_id = currentUserId),
                        
                        // Expense Categories (6)
                        CategoryEntity(name = "Food & Dining", type = "Expense", color = "#FF9500", user_id = currentUserId),
                        CategoryEntity(name = "Transportation", type = "Expense", color = "#5AC8FA", user_id = currentUserId),
                        CategoryEntity(name = "Housing & Utilities", type = "Expense", color = "#FF3B30", user_id = currentUserId),
                        CategoryEntity(name = "Shopping", type = "Expense", color = "#AF52DE", user_id = currentUserId),
                        CategoryEntity(name = "Healthcare", type = "Expense", color = "#FF2D55", user_id = currentUserId),
                        CategoryEntity(name = "Other Expenses", type = "Expense", color = "#8E8E93", user_id = currentUserId)
                    )
                    defaultCategories.forEach { category ->
                        categoryDao.insertCategory(category)
                    }
                }
            }
        }
    }
    fun initType(type: String) {
        _isIncome.value = type.lowercase() == "income"
    }

    fun initForEdit(transactionId: Int) {
        viewModelScope.launch {
            transactionDao.getTransactionById(transactionId).firstOrNull()?.let { transaction ->
                _editingTransactionId.value = transaction.transaction_id
                _amount.value = transaction.amount.toString()
                _note.value = transaction.note
                _isIncome.value = transaction.type == "Income"
                
                categoryDao.getCategoryById(transaction.category_id).firstOrNull()?.let { category ->
                    _selectedCategory.value = category
                }
            }
        }
    }
}
