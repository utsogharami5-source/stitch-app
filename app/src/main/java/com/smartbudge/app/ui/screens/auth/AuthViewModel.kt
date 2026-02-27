package com.smartbudge.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userDao: com.smartbudge.app.data.local.dao.UserDao,
    private val networkUtils: com.smartbudge.app.util.NetworkUtils,
    private val backupRepository: com.smartbudge.app.data.repository.BackupRepository
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    init {
        // Check if user is already logged in
        val user = auth.currentUser
        if (user != null) {
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email: String, pass: String, name: String = "") {
        if (!networkUtils.isNetworkAvailable()) {
            _authState.value = AuthState.Error("No Internet Connection. Please check your network and try again.")
            return
        }
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email.trim(), pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        viewModelScope.launch {
                            // First, try to restore cloud data
                            backupRepository.downloadData()
                            // If a name was provided during login, update it
                            if (name.isNotBlank()) {
                                saveUserToDb(user.uid, name, email)
                                backupRepository.uploadUserProfile()
                            }
                            _authState.value = AuthState.Authenticated
                        }
                    } else {
                        _authState.value = AuthState.Authenticated
                    }
                } else {
                    val error = task.exception?.message ?: "Login failed"
                    _authState.value = if (error.contains("restricted to administrators")) {
                        AuthState.Error("Firebase Auth Error: Please enable 'Email/Password' in your Firebase Console.")
                    } else AuthState.Error(error)
                }
            }
    }

    fun signUp(email: String, pass: String, name: String) {
        if (!networkUtils.isNetworkAvailable()) {
            _authState.value = AuthState.Error("No Internet Connection. Please check your network and try again.")
            return
        }
        if (email.isBlank() || pass.isBlank() || name.isBlank()) {
            _authState.value = AuthState.Error("All fields are required")
            return
        }
        if (pass.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email.trim(), pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        viewModelScope.launch {
                            saveUserToDb(user.uid, name, email)
                            backupRepository.uploadUserProfile()
                            user.sendEmailVerification()
                            _authState.value = AuthState.Authenticated
                        }
                    } else {
                        _authState.value = AuthState.Authenticated
                    }
                } else {
                    val error = task.exception?.message ?: "Signup failed"
                    _authState.value = if (error.contains("restricted to administrators")) {
                        AuthState.Error("Firebase Auth Error: Please enable 'Email/Password' in your Firebase Console.")
                    } else AuthState.Error(error)
                }
            }
    }

    fun resetPassword(email: String) {
        if (!networkUtils.isNetworkAvailable()) {
            _authState.value = AuthState.Error("No Internet Connection. Please check your network to reset password.")
            return
        }
        if (email.isBlank()) {
            _authState.value = AuthState.Error("Please enter your email")
            return
        }
        _authState.value = AuthState.Loading
        auth.sendPasswordResetEmail(email.trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success("Password reset email sent")
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Failed to send reset email")
                }
            }
    }

    fun registerWithName(name: String) {
        if (!networkUtils.isNetworkAvailable()) {
            _authState.value = AuthState.Error("No Internet Connection. Please check your network and try again.")
            return
        }
        if (name.isBlank()) {
            _authState.value = AuthState.Error("Please enter a name")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    viewModelScope.launch {
                        saveUserToDb(user.uid, name, "")
                        backupRepository.uploadUserProfile()
                        _authState.value = AuthState.Authenticated
                    }
                } else {
                    _authState.value = AuthState.Authenticated
                }
            } else {
                val error = task.exception?.message ?: "Registration failed"
                _authState.value = if (error.contains("restricted to administrators")) {
                    AuthState.Error("Firebase Auth Error: Please enable 'Anonymous' authentication in your Firebase Console.")
                } else AuthState.Error(error)
            }
        }
    }

    fun isUserAnonymous(): Boolean {
        return auth.currentUser?.isAnonymous ?: true
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    private fun saveUserToDb(uid: String, name: String, email: String) {
        viewModelScope.launch {
            userDao.insertUser(
                com.smartbudge.app.data.local.entity.UserEntity(
                    user_id = uid,
                    name = name,
                    email = email,
                    monthly_budget = 0.0,
                    created_at = System.currentTimeMillis()
                )
            )
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }
    
    fun resetState() {
        if (_authState.value !is AuthState.Authenticated) {
            _authState.value = AuthState.Idle
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val error: String) : AuthState()
}
