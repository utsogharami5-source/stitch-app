package com.smartbudge.app.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartbudge.app.ui.components.PremiumCard
import com.smartbudge.app.ui.components.PremiumButton
import com.smartbudge.app.ui.components.SmartTextField
import com.smartbudge.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(
    isLoggedIn: Boolean = false,
    isAnonymous: Boolean = true,
    onLoginRequired: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val backupStatus by viewModel.backupStatus.collectAsState()
    val isDark = true
    
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var showNoInternetDialog by remember { mutableStateOf(false) }

    val bgColor = if (isDark) BackgroundDark else BackgroundLight
    val textColor = if (isDark) TextDark else TextLight
    val mutedTextColor = if (isDark) MutedTextDark else MutedTextLight
    val surfaceColor = if (isDark) CardDark else CardLight
    
    val context = androidx.compose.ui.platform.LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    val checkingStatus by viewModel.updateCheckStatus.collectAsState()

    LaunchedEffect(backupStatus, checkingStatus) {
        if (backupStatus?.contains("No Internet") == true || 
            checkingStatus?.contains("No Internet") == true) {
            showNoInternetDialog = true
        } else if (backupStatus != null) {
            snackbarHostState.showSnackbar(backupStatus!!)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = bgColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Profile & Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // User Info Section
            PremiumCard(
                modifier = Modifier.fillMaxWidth(),
                isGlass = true,
                bgColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f),
                padding = 16.dp,
                cornerRadius = 24.dp
            ) {
                ProfileItem(
                    label = "Name",
                    value = user?.name ?: "Set Name",
                    textColor = textColor,
                    onClick = { showNameDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Cloud Backup",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            PremiumCard(
                modifier = Modifier.fillMaxWidth(),
                isGlass = true,
                bgColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f),
                padding = 16.dp,
                cornerRadius = 24.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Sync with Firebase", fontWeight = FontWeight.Medium, color = textColor)
                            val lastBackup by viewModel.lastBackupTime.collectAsState()
                            val lastBackupText = if (lastBackup > 0) {
                                val sdf = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
                                "Last Backup: ${sdf.format(java.util.Date(lastBackup))}"
                            } else {
                                "Backup your data to the cloud"
                            }
                            Text(text = lastBackupText, fontSize = 12.sp, color = mutedTextColor)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { 
                                if (!isLoggedIn) onLoginRequired() else viewModel.downloadBackup() 
                            }
                        ) {
                            Text("Restore Data", color = PrimaryBlue, fontSize = 14.sp)
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        val isBackupSuccess = backupStatus == "Backup Successful"
                        PremiumButton(
                            text = if (isBackupSuccess) "Backup Completed" else "Backup Now",
                            onClick = {
                                if (!isLoggedIn) {
                                    onLoginRequired()
                                } else if (!isBackupSuccess) {
                                    viewModel.uploadBackup() 
                                }
                            },
                            colors = if (isBackupSuccess) listOf(Color(0xFF34C759), Color(0xFF248A3D)) else PrimaryGradient,
                            modifier = Modifier.height(44.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logout Button - check isLoggedIn carefully
            if (isLoggedIn) {
                PremiumButton(
                    text = "Sign Out Account",
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = listOf(iOSRed, iOSRed.copy(alpha = 0.7f))
                )
            } else {
                PremiumButton(
                    text = "Sign In / Register",
                    onClick = onLoginRequired,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "App Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            PremiumCard(
                modifier = Modifier.fillMaxWidth(),
                isGlass = true,
                bgColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f),
                padding = 16.dp,
                cornerRadius = 24.dp
            ) {
                ProfileItem(label = "Currency", value = "BDT (৳)", textColor = textColor, onClick = {})
                Spacer(modifier = Modifier.height(8.dp))
                
                // Add Check for Updates button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.checkForUpdates(context) }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "App Version", fontSize = 16.sp, color = textColor)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "1.0.8", fontSize = 16.sp, color = iOSBlue, fontWeight = FontWeight.Medium)
                        val checkingStatus = viewModel.updateCheckStatus.collectAsState().value
                        if (checkingStatus != null && !checkingStatus.contains("1.0.8")) {
                            Text(text = checkingStatus, fontSize = 12.sp, color = mutedTextColor)
                        }
                    }
                }
            }

        }
    }


    if (showNameDialog) {
        EditValueDialog(
            title = "Update Name",
            initialValue = user?.name ?: "",
            onDismiss = { showNameDialog = false },
            onSave = {
                viewModel.updateName(it)
                showNameDialog = false
            }
        )
    }

    if (showNoInternetDialog) {
        AlertDialog(
            onDismissRequest = { showNoInternetDialog = false },
            title = { Text("Offline - No Internet", fontWeight = FontWeight.Bold, color = textColor) },
            text = { Text("Internet is required to backup or restore your data cloud. Please check your connection and try again.", color = mutedTextColor) },
            confirmButton = {
                Button(
                    onClick = { showNoInternetDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = com.smartbudge.app.ui.theme.PrimaryBlue)
                ) {
                    Text("OK")
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = surfaceColor
        )
    }
}

@Composable
fun ProfileItem(label: String, value: String, textColor: Color = Color.Unspecified, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 16.sp, color = textColor)
        Text(text = value, fontSize = 16.sp, color = iOSBlue, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun EditValueDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var text by remember { mutableStateOf(initialValue) }
    Dialog(onDismissRequest = onDismiss) {
        PremiumCard(
            modifier = Modifier.fillMaxWidth(),
            isGlass = true,
            bgColor = Color.White.copy(alpha = 0.1f), // Better contrast for dialogs
            padding = 20.dp,
            cornerRadius = 28.dp
        ) {
            Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            SmartTextField(
                value = text,
                onValueChange = { text = it },
                label = "Value",
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onSave(text) }, 
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save")
                }
            }
        }
    }
}
