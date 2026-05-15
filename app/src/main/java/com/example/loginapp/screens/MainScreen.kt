package com.example.loginapp.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// Các tab trong bottom navigation
sealed class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    object Home    : BottomTab("tab_home",    "Trang chủ", Icons.Default.Home)
    object Movies  : BottomTab("tab_movies",  "Phim",      Icons.Default.VideoLibrary)
    object Profile : BottomTab("tab_profile", "Hồ sơ",     Icons.Default.Person)
}

private val bottomTabs = listOf(BottomTab.Home, BottomTab.Movies, BottomTab.Profile)

// Màn hình chính chứa BottomNavigationBar — được gắn sau khi đăng nhập thành công
@Composable
fun MainScreen(
    displayName: String?,
    userEmail: String?,
    infoMessage: String?,
    onMovieClick: (String) -> Unit,
    onSignOutClick: () -> Unit
) {
    val innerNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            NavigationBar {
                bottomTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                        onClick = {
                            innerNavController.navigate(tab.route) {
                                // Không chồng chất back stack khi bấm lại cùng tab
                                popUpTo(innerNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                        label = { Text(text = tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = innerNavController,
            startDestination = BottomTab.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomTab.Home.route) {
                HomeScreen(
                    displayName = displayName,
                    userEmail = userEmail,
                    infoMessage = infoMessage,
                    onMovieClick = onMovieClick,
                    onSignOutClick = onSignOutClick
                )
            }

            composable(BottomTab.Movies.route) {
                MovieListScreen(onMovieClick = onMovieClick)
            }

            composable(BottomTab.Profile.route) {
                ProfileScreen(
                    displayName = displayName,
                    userEmail = userEmail,
                    onSignOutClick = onSignOutClick
                )
            }
        }
    }
}

