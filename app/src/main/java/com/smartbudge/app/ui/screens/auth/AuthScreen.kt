package com.smartbudge.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartbudge.app.ui.components.*
import com.smartbudge.app.ui.theme.*
import com.smartbudge.app.ui.components.*

@Composable
fun AuthScreen(
    onNavigateToMain: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var authMode by remember { mutableStateOf<AuthMode>(AuthMode.QuickStart) }
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated || authState is AuthState.Success) {
            onNavigateToMain()
            viewModel.resetState()
        }
    }

    val isDark = true
    val backgroundColor = if (isDark) BackgroundDark else BackgroundLight
    val surfaceColor = if (isDark) CardDark else CardLight
    val textColor = if (isDark) TextDark else TextLight
    val mutedTextColor = if (isDark) MutedTextDark else MutedTextLight


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Header
        Icon(
            imageVector = Icons.Default.AccountBalanceWallet,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "SmartBudge",
            style = MaterialTheme.typography.displayLarge,
            color = PrimaryBlue
        )
        Text(
            text = "Streamlined Financial Tracking",
            style = MaterialTheme.typography.bodyLarge,
            color = mutedTextColor
        )

        Spacer(modifier = Modifier.height(64.dp))

        when (authMode) {
            AuthMode.QuickStart -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Quick Start",
                        style = MaterialTheme.typography.titleLarge,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enter your name to start tracking immediately.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = mutedTextColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    TextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        placeholder = { Text("How should we call you?", color = mutedTextColor.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = surfaceColor,
                            unfocusedContainerColor = surfaceColor,
                            focusedIndicatorColor = PrimaryBlue,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    PremiumButton(
                        text = "Start Now",
                        onClick = { viewModel.registerWithName(displayName) },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { authMode = AuthMode.Login }) {
                            Text("Login", style = MaterialTheme.typography.labelMedium, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                        }
                        Text("•", color = mutedTextColor, modifier = Modifier.padding(horizontal = 8.dp))
                        TextButton(onClick = { authMode = AuthMode.SignUp }) {
                            Text("Create Account", style = MaterialTheme.typography.labelMedium, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            AuthMode.Login -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Welcome Back",
                        style = MaterialTheme.typography.titleLarge,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Email Address", color = mutedTextColor.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryBlue) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = surfaceColor,
                            unfocusedContainerColor = surfaceColor,
                            focusedIndicatorColor = PrimaryBlue,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Password", color = mutedTextColor.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryBlue) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = surfaceColor,
                            unfocusedContainerColor = surfaceColor,
                            focusedIndicatorColor = PrimaryBlue,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    PremiumButton(
                        text = "Login",
                        onClick = { viewModel.login(email, password) },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    )
                    
                    TextButton(onClick = { authMode = AuthMode.QuickStart }) {
                        Text("Back to Quick Start", style = MaterialTheme.typography.labelMedium, color = mutedTextColor)
                    }
                }
            }
            AuthMode.SignUp -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.titleLarge,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    TextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        placeholder = { Text("Full Name", color = mutedTextColor.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = surfaceColor,
                            unfocusedContainerColor = surfaceColor,
                            focusedIndicatorColor = PrimaryBlue,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Email Address", color = mutedTextColor.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryBlue) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = surfaceColor,
                            unfocusedContainerColor = surfaceColor,
                            focusedIndicatorColor = PrimaryBlue,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Create Password", color = mutedTextColor.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryBlue) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = surfaceColor,
                            unfocusedContainerColor = surfaceColor,
                            focusedIndicatorColor = PrimaryBlue,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    PremiumButton(
                        text = "Create Account",
                        onClick = { viewModel.signUp(email, password, displayName) },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    )
                    
                    TextButton(onClick = { authMode = AuthMode.QuickStart }) {
                        Text("Back to Quick Start", style = MaterialTheme.typography.labelMedium, color = mutedTextColor)
                    }
                }
            }
        }

        if (authState is AuthState.Error && !(authState as AuthState.Error).error.contains("No Internet")) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (authState as AuthState.Error).error,
                color = iOSRed,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
        
        if (authState is AuthState.Loading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(color = PrimaryBlue, strokeWidth = 3.dp)
        }
    }
}

enum class AuthMode {
    QuickStart, Login, SignUp
}
