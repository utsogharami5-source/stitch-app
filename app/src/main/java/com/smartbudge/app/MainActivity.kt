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
import dagger.hilt.android.AndroidEntryPoint

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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
