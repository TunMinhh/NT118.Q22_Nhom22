package com.example.loginapp.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.loginapp.auth.AuthViewModel
import androidx.compose.runtime.setValue

// Các tab trong bottom navigation
sealed class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    object Home    : BottomTab("tab_home",    "Trang chủ", Icons.Default.Home)
    object Movies  : BottomTab("tab_movies",  "Phim",      Icons.Default.VideoLibrary)
    object Cinema  : BottomTab("tab_cinema",  "Rạp",       Icons.Default.LocationOn)
    object Profile : BottomTab("tab_profile", "Hồ sơ",     Icons.Default.Person)
}

private val bottomTabs = listOf(BottomTab.Home, BottomTab.Movies, BottomTab.Cinema, BottomTab.Profile)

// Màn hình chính chứa BottomNavigationBar — được gắn sau khi đăng nhập thành công
@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    onMovieClick: (String) -> Unit,
    onCinemaShowtimeClick: (movieId: String, showtimeId: String) -> Unit,
    onSignOutClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val uiState = authViewModel.uiState
    val innerNavController = rememberNavController()
    var showBottomBar by remember { mutableStateOf(true) }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
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
        }
    ) { innerPadding ->
        NavHost(
            navController = innerNavController,
            startDestination = BottomTab.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomTab.Home.route) {
                HomeScreen(
                    displayName = uiState.displayName,
                    userEmail = uiState.userEmail,
                    infoMessage = uiState.infoMessage,
                    onMovieClick = onMovieClick,
                    onSignOutClick = onSignOutClick,


                    onSearchStateChange = { isSearching ->
                        showBottomBar = !isSearching
                    }
                )
            }

            composable(BottomTab.Movies.route) {
                MovieListScreen(onMovieClick = onMovieClick)
            }

            composable(BottomTab.Cinema.route) {
                CinemaScreen(onShowtimeClick = onCinemaShowtimeClick)
            }

            composable(BottomTab.Profile.route) {
                ProfileScreen(
                    uiState = uiState,
                    onUpdateProfile = { name, phone, address, onComplete ->
                        authViewModel.updateUserProfile(name, phone, address, onComplete)
                    },
                    onSignOutClick = onSignOutClick,
                    onHistoryClick = onHistoryClick
                )
            }
        }
    }
}

