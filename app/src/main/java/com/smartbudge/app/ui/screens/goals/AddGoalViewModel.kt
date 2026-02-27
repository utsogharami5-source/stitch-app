package com.smartbudge.app.ui.screens.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudge.app.data.local.dao.SavingsGoalDao
import com.smartbudge.app.data.local.entity.SavingsGoalEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddGoalViewModel @Inject constructor(
    private val savingsGoalDao: SavingsGoalDao
) : ViewModel() {

    private val userId: String
        get() = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _targetAmount = MutableStateFlow("")
    val targetAmount: StateFlow<String> = _targetAmount.asStateFlow()

    fun setTitle(value: String) {
        _title.value = value
    }

    fun setTargetAmount(value: String) {
        _targetAmount.value = value
    }

    fun saveGoal(onSuccess: () -> Unit) {
        val amount = _targetAmount.value.toDoubleOrNull() ?: return
        if (_title.value.isBlank()) return

        val goal = SavingsGoalEntity(
            user_id = userId,
            title = _title.value,
            target_amount = amount,
            saved_amount = 0.0,
            deadline = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000) // Default 30 days
        )

        viewModelScope.launch {
            savingsGoalDao.insertGoal(goal)
            onSuccess()
        }
    }
}
