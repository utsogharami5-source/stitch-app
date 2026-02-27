package com.smartbudge.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.smartbudge.app.ui.navigation.AppNavigation
import com.smartbudge.app.ui.theme.SmartBudgeTheme
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.smartbudge.app.updater.GitHubUpdateManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable High Refresh Rate (60fps, 90fps, 120fps+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.windowManager.defaultDisplay.supportedModes.maxByOrNull { it.refreshRate }?.let {
                window.attributes = window.attributes.apply {
                    preferredDisplayModeId = it.modeId
                }
            }
        }

        setContent {
            SmartBudgeTheme {
                var updateInfo by mutableStateOf<GitHubUpdateManager.ReleaseInfo?>(null)
                val coroutineScope = rememberCoroutineScope()
                val updateManager = GitHubUpdateManager(this@MainActivity)

                // Check for updates periodically when app starts
                LaunchedEffect(Unit) {
                    try {
                        // Using BuildConfig.VERSION_NAME requires gradle setup; fallback to package manager
                        val packageInfo = packageManager.getPackageInfo(packageName, 0)
                        val versionName = packageInfo.versionName ?: "1.0.0"
                        
                        updateInfo = updateManager.checkForUpdates(versionName)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                    
                    // Show update dialog if available
                    updateInfo?.let { release ->
                        AlertDialog(
                            onDismissRequest = { updateInfo = null },
                            title = { Text("Update Available") },
                            text = { 
                                Text("Version ${release.versionName} is now available!\n\n" +
                                     "What's new:\n" +
                                     "${release.releaseNotes}") 
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    updateInfo = null
                                    updateManager.downloadAndInstallUpdate(release)
                                }) {
                                    Text("Download & Install")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { updateInfo = null }) {
                                    Text("Later")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
