package com.smartbudge.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudge.app.data.local.dao.UserDao
import com.smartbudge.app.data.local.entity.UserEntity
import com.smartbudge.app.data.repository.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userDao: UserDao,
    private val backupRepository: BackupRepository,
    private val networkUtils: com.smartbudge.app.util.NetworkUtils,
    private val preferenceRepository: com.smartbudge.app.data.repository.PreferenceRepository
) : ViewModel() {

    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    
    private val currentUserId: String
        get() = auth.currentUser?.uid ?: "anonymous"

    val user: StateFlow<UserEntity?> = userDao.getUser(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isDarkMode: StateFlow<Boolean> = preferenceRepository.isDarkMode

    private val _backupStatus = MutableStateFlow<String?>(null)
    val backupStatus: StateFlow<String?> = _backupStatus.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        preferenceRepository.setDarkMode(enabled)
    }

    fun updateMonthlyBudget(amount: Double) {
        viewModelScope.launch {
            val currentUser = user.value
            if (currentUser != null) {
                userDao.updateUser(currentUser.copy(monthly_budget = amount))
            } else {
                userDao.insertUser(
                    UserEntity(
                        user_id = currentUserId,
                        name = "User",
                        email = "",
                        monthly_budget = amount,
                        created_at = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun updateName(name: String) {
        viewModelScope.launch {
            val currentUser = user.value
            if (currentUser != null) {
                userDao.updateUser(currentUser.copy(name = name))
            } else {
                userDao.insertUser(
                    UserEntity(
                        user_id = currentUserId,
                        name = name,
                        email = "",
                        monthly_budget = 0.0,
                        created_at = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun uploadBackup() {
        if (!networkUtils.isNetworkAvailable()) {
            _backupStatus.value = "No Internet Connection. Please connect to Wi-Fi or data."
            return
        }
        viewModelScope.launch {
            _backupStatus.value = "Uploading..."
            val result = backupRepository.uploadData()
            _backupStatus.value = if (result.isSuccess) "Backup Successful" else "Backup Failed: ${result.exceptionOrNull()?.message}"
        }
    }

    fun downloadBackup() {
        if (!networkUtils.isNetworkAvailable()) {
            _backupStatus.value = "No Internet Connection. Please connect to Wi-Fi or data."
            return
        }
        viewModelScope.launch {
            _backupStatus.value = "Downloading..."
            val result = backupRepository.downloadData()
            _backupStatus.value = if (result.isSuccess) "Restore Successful" else "Restore Failed: ${result.exceptionOrNull()?.message}"
        }
    }

    private val _updateCheckStatus = MutableStateFlow<String?>(null)
    val updateCheckStatus: StateFlow<String?> = _updateCheckStatus.asStateFlow()

    fun checkForUpdates(context: android.content.Context) {
        viewModelScope.launch {
            val updateManager = com.smartbudge.app.updater.GitHubUpdateManager(context)
            if (!updateManager.isNetworkAvailable()) {
                _updateCheckStatus.value = "No Internet Connection"
                kotlinx.coroutines.delay(2000)
                _updateCheckStatus.value = null
                return@launch
            }

            _updateCheckStatus.value = "Checking..."
            try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val currentVersion = packageInfo.versionName ?: "1.0.0"
                
                val releaseInfo = updateManager.checkForUpdates(currentVersion)
                if (releaseInfo != null) {
                    _updateCheckStatus.value = "Update Available (${releaseInfo.versionName})"
                    if (!updateManager.hasStoragePermission()) {
                        _updateCheckStatus.value = "Storage Permission Required"
                        android.widget.Toast.makeText(context, "Please grant storage permission in settings to update.", android.widget.Toast.LENGTH_LONG).show()
                    } else if (updateManager.canInstallPackages()) {
                        android.widget.Toast.makeText(context, "Starting Download...", android.widget.Toast.LENGTH_SHORT).show()
                        updateManager.downloadAndInstallUpdate(releaseInfo)
                    } else {
                        android.widget.Toast.makeText(context, "Please allow installation from unknown sources", android.widget.Toast.LENGTH_LONG).show()
                        updateManager.requestInstallPermission()
                    }
                } else {
                    _updateCheckStatus.value = "Up to date"
                    kotlinx.coroutines.delay(2000)
                    _updateCheckStatus.value = null
                }
            } catch (e: Exception) {
                _updateCheckStatus.value = "Check failed"
                kotlinx.coroutines.delay(2000)
                _updateCheckStatus.value = null
            }
        }
    }
}
