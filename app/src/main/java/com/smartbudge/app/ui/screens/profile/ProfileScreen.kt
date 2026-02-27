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

    LaunchedEffect(backupStatus) {
        backupStatus?.let {
            if (it.contains("No Internet")) {
                showNoInternetDialog = true
            } else {
                snackbarHostState.showSnackbar(it)
            }
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Sync with Firebase", fontWeight = FontWeight.Medium, color = textColor)
                        Text(text = "Backup your data to the cloud", fontSize = 12.sp, color = mutedTextColor)
                    }
                    Row {
                        TextButton(
                            onClick = { 
                                if (!isLoggedIn) onLoginRequired() else viewModel.downloadBackup() 
                            }
                        ) {
                            Text("Restore", color = PrimaryBlue)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        val isBackupSuccess = backupStatus == "Backup Successful"
                        Button(
                            onClick = {
                                if (!isLoggedIn) {
                                    onLoginRequired()
                                } else if (!isBackupSuccess) {
                                    viewModel.uploadBackup() 
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isBackupSuccess) com.smartbudge.app.ui.theme.CardLight else PrimaryBlue
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isBackupSuccess) "Backup Completed" else "Backup", color = if (isBackupSuccess) com.smartbudge.app.ui.theme.PrimaryBlue else Color.White)
                        }
                    }
                }
            }

            if (isLoggedIn) {
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = iOSRed.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
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
                    val checkingStatus = viewModel.updateCheckStatus.collectAsState().value
                    Text(
                        text = checkingStatus ?: "Check for Updates", 
                        fontSize = 16.sp, 
                        color = if (checkingStatus == null) com.smartbudge.app.ui.theme.PrimaryBlue else mutedTextColor, 
                        fontWeight = FontWeight.Medium
                    )
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
            bgColor = Color.Black.copy(alpha = 0.05f), // Subtly different for dialogs
            padding = 24.dp
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
