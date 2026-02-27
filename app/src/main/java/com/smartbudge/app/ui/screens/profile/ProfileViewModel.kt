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
import com.smartbudge.app.updater.GitHubUpdateManager

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
    val lastBackupTime: StateFlow<Long> = preferenceRepository.lastBackupTime

    private val _backupStatus = MutableStateFlow<String?>(null)
    val backupStatus: StateFlow<String?> = _backupStatus.asStateFlow()

    private val _updateCheckStatus = MutableStateFlow<String?>(null)
    val updateCheckStatus: StateFlow<String?> = _updateCheckStatus.asStateFlow()

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
            backupRepository.uploadUserProfile()
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
            backupRepository.uploadUserProfile()
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
            if (result.isSuccess) {
                val time = System.currentTimeMillis()
                preferenceRepository.setLastBackupTime(time)
                _backupStatus.value = "Backup Successful"
            } else {
                _backupStatus.value = "Backup Failed: ${result.exceptionOrNull()?.message}"
            }
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
            if (result.isSuccess) {
                _backupStatus.value = "Restore Successful"
            } else {
                _backupStatus.value = "Restore Failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    private val _updateInfo = MutableStateFlow<GitHubUpdateManager.ReleaseInfo?>(null)
    val updateInfo: StateFlow<GitHubUpdateManager.ReleaseInfo?> = _updateInfo.asStateFlow()

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
                _updateInfo.value = releaseInfo
                
                if (releaseInfo != null) {
                    _updateCheckStatus.value = "Update Available (${releaseInfo.versionName})"
                    // If permissions are ready, we can offer to start it
                    // But usually, manual check just shows the status
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

    fun openReleasePage(context: android.content.Context) {
        _updateInfo.value?.let { info ->
            com.smartbudge.app.updater.GitHubUpdateManager(context).openUrl(info.releaseUrl)
        }
    }

    fun startUpdate(context: android.content.Context) {
        _updateInfo.value?.let { info ->
            val updateManager = com.smartbudge.app.updater.GitHubUpdateManager(context)
            if (!updateManager.hasStoragePermission()) {
                _updateCheckStatus.value = "Storage Permission Required"
            } else if (updateManager.canInstallPackages()) {
                updateManager.downloadAndInstallUpdate(info)
            } else {
                updateManager.requestInstallPermission()
            }
        }
    }
}
