package com.smartbudge.app

import androidx.lifecycle.ViewModel
import com.smartbudge.app.data.repository.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {
    val isDarkMode: StateFlow<Boolean> = preferenceRepository.isDarkMode
}
