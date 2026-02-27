package com.smartbudge.app.ui.navigation

import androidx.navigation.*
import androidx.navigation.compose.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.smartbudge.app.ui.screens.auth.AuthViewModel
import com.smartbudge.app.ui.screens.auth.AuthState
import com.smartbudge.app.ui.screens.auth.AuthScreen
import com.smartbudge.app.ui.screens.home.HomeScreen
import com.smartbudge.app.ui.screens.add.AddTransactionScreen
import com.smartbudge.app.ui.screens.reports.ReportsScreen
import com.smartbudge.app.ui.screens.goals.GoalsScreen
import com.smartbudge.app.ui.screens.profile.ProfileScreen
import com.smartbudge.app.ui.screens.category.CategoryModuleScreen
import com.smartbudge.app.ui.screens.details.TransactionDetailsScreen
import com.smartbudge.app.ui.components.SmartBottomBar
import com.smartbudge.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(authViewModel: AuthViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define routes where bottom bar should be shown
    val showBottomBar = currentRoute in listOf("home", "reports", "goals", "profile")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                SmartBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (authState is AuthState.Authenticated) "home" else "auth",
            modifier = Modifier.padding(paddingValues)
        ) {
            // Auth Flow
            composable("auth") {
                AuthScreen(
                    onNavigateToMain = {
                        navController.navigate("home") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }

            // Main Flow
            composable("home") {
                HomeScreen(
                    onNavigateToAddTransaction = { type, id ->
                        if (id != null) {
                            navController.navigate("add_transaction/$type?transactionId=$id")
                        } else {
                            navController.navigate("add_transaction/$type")
                        }
                    },
                    onNavigateToTransactionDetails = { id ->
                        navController.navigate("transaction_details/$id")
                    },
                    onOpenDrawer = { /* Drawer removed */ }
                )
            }
            composable(
                "add_transaction/{type}?transactionId={transactionId}",
                arguments = listOf(
                    navArgument("type") { defaultValue = "expense" },
                    navArgument("transactionId") { 
                        type = NavType.IntType
                        defaultValue = -1 
                    }
                )
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: "expense"
                val transactionId = backStackEntry.arguments?.getInt("transactionId") ?: -1
                AddTransactionScreen(
                    type = type,
                    transactionId = if (transactionId != -1) transactionId else null,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("reports") {
                ReportsScreen()
            }

            composable("goals") {
                GoalsScreen()
            }

            composable(
                "category/{name}",
                arguments = listOf(androidx.navigation.navArgument("name") { type = NavType.StringType })
            ) { backStackEntry ->
                val name = backStackEntry.arguments?.getString("name") ?: ""
                CategoryModuleScreen(
                    categoryName = name,
                    onNavigateToTransactionDetails = { id ->
                        navController.navigate("transaction_details/$id")
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "transaction_details/{transactionId}",
                arguments = listOf(
                    navArgument("transactionId") { type = NavType.IntType }
                )
            ) {
                TransactionDetailsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { type, id -> 
                        navController.navigate("add_transaction/$type?transactionId=$id") {
                            // Pop out of details screen so when user saves in AddTransactionScreen they return to Home/Category
                            popUpTo("transaction_details/{transactionId}") { inclusive = true }
                        }
                    }
                )
            }

            composable("profile") {
                ProfileScreen(
                    isLoggedIn = authViewModel.isLoggedIn(),
                    isAnonymous = authViewModel.isUserAnonymous(),
                    onLoginRequired = {
                        authViewModel.logout()
                        navController.navigate("auth")
                    },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

// DrawerContent and DrawerItem removed as side drawer is no longer needed.

// Side drawer logic removed. Navigation is now handled by the bottom bar.
