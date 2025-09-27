package com.constructionmanager.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.constructionmanager.ui.screens.dashboard.DashboardScreen
import com.constructionmanager.ui.screens.materials.MaterialsScreen
import com.constructionmanager.ui.screens.projects.ProjectsScreen

@Composable
fun ConstructionManagerNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "dashboard",
        modifier = modifier.fillMaxSize()
    ) {
        composable("dashboard") {
            DashboardScreen(
                onNavigateToProjects = {
                    navController.navigate("projects")
                },
                onNavigateToMaterials = {
                    navController.navigate("materials")
                }
            )
        }
        
        composable("projects") {
            ProjectsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("materials") {
            MaterialsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}