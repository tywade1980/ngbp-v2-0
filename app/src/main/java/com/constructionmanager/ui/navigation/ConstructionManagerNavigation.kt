package com.constructionmanager.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.constructionmanager.ui.screens.auth.LoginScreen
import com.constructionmanager.ui.screens.dashboard.DashboardScreen
import com.constructionmanager.ui.screens.documentation.PhotoDocumentationScreen
import com.constructionmanager.ui.screens.labor.LaborManagementScreen
import com.constructionmanager.ui.screens.materials.MaterialsScreen
import com.constructionmanager.ui.screens.projects.ProjectDetailsScreen
import com.constructionmanager.ui.screens.projects.ProjectsScreen
import com.constructionmanager.ui.screens.reports.ReportsScreen
import com.constructionmanager.ui.screens.settings.SettingsScreen
import com.constructionmanager.ui.screens.workflows.WorkflowScreen

@Composable
fun ConstructionManagerNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    isAuthenticated: Boolean = true
) {
    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) "dashboard" else "auth/login",
        modifier = modifier.fillMaxSize()
    ) {
        // Authentication
        composable("auth/login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("auth/login") { inclusive = true }
                    }
                }
            )
        }
        
        // Main Dashboard
        composable("dashboard") {
            DashboardScreen(
                onNavigateToProjects = {
                    navController.navigate("projects")
                },
                onNavigateToMaterials = {
                    navController.navigate("materials")
                },
                onNavigateToLabor = {
                    navController.navigate("labor")
                },
                onNavigateToWorkflows = {
                    navController.navigate("workflows")
                },
                onNavigateToReports = {
                    navController.navigate("reports")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }
        
        // Projects
        composable("projects") {
            ProjectsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToProject = { projectId ->
                    navController.navigate("projects/$projectId")
                }
            )
        }
        
        composable(
            "projects/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            ProjectDetailsScreen(
                projectId = projectId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDocumentation = {
                    navController.navigate("projects/$projectId/documentation")
                },
                onNavigateToWorkflows = {
                    navController.navigate("projects/$projectId/workflows")
                }
            )
        }
        
        // Materials
        composable("materials") {
            MaterialsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Labor Management
        composable("labor") {
            LaborManagementScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Workflows
        composable("workflows") {
            WorkflowScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                projectId = null
            )
        }
        
        composable(
            "projects/{projectId}/workflows",
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            WorkflowScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                projectId = projectId
            )
        }
        
        // Photo Documentation
        composable(
            "projects/{projectId}/documentation",
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            PhotoDocumentationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                projectId = projectId
            )
        }
        
        // Reports
        composable("reports") {
            ReportsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Settings
        composable("settings") {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate("auth/login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }
    }
}