package com.smartbudge.app.ui.screens.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudge.app.data.local.dao.SavingsGoalDao
import com.smartbudge.app.data.local.entity.SavingsGoalEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val savingsGoalDao: SavingsGoalDao
) : ViewModel() {

    private val userId: String
        get() = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

    val savingsGoals: StateFlow<List<SavingsGoalEntity>> = savingsGoalDao
        .getSavingsGoals(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
