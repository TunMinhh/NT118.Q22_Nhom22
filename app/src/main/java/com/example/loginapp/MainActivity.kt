package com.example.loginapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.loginapp.auth.AuthViewModel
import com.example.loginapp.screens.ForgotPasswordScreen
import com.example.loginapp.screens.HomeScreen
import com.example.loginapp.screens.LoginScreen
import com.example.loginapp.screens.SignUpScreen
import com.example.loginapp.ui.theme.LoginAppTheme

// Khai báo các route dùng để điều hướng giữa các màn hình
private object AppRoute {
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val FORGOT_PASSWORD = "forgot_password"
    const val HOME = "home"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoginAppTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel = viewModel()) {
    // Tạo bộ điều hướng trung tâm cho ứng dụng
    val navController = rememberNavController()
    val uiState = authViewModel.uiState

    NavHost(
        navController = navController,
        startDestination = if (uiState.isAuthenticated) AppRoute.HOME else AppRoute.LOGIN
    ) {
        composable(AppRoute.LOGIN) {
            LoginScreen(
                onSignUpClick = {
                    authViewModel.clearFeedback()
                    navController.navigate(AppRoute.SIGN_UP)
                },
                onForgotPasswordClick = {
                    authViewModel.clearFeedback()
                    navController.navigate(AppRoute.FORGOT_PASSWORD)
                },
                onLoginClick = { email, password ->
                    authViewModel.login(email, password) {
                        navController.navigate(AppRoute.HOME) {
                            popUpTo(AppRoute.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                infoMessage = uiState.infoMessage
            )
        }

        composable(AppRoute.SIGN_UP) {
            SignUpScreen(
                onBackClick = {
                    authViewModel.clearFeedback()
                    navController.popBackStack()
                },
                onSignUpClick = { fullName, email, password, confirmPassword ->
                    authViewModel.signUp(fullName, email, password, confirmPassword) {
                        navController.navigate(AppRoute.HOME) {
                            popUpTo(AppRoute.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage
            )
        }

        composable(AppRoute.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBackClick = {
                    authViewModel.clearFeedback()
                    navController.popBackStack()
                },
                onResetPasswordClick = { email ->
                    authViewModel.resetPassword(email)
                },
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                infoMessage = uiState.infoMessage
            )
        }

        composable(AppRoute.HOME) {
            HomeScreen(
                displayName = uiState.displayName,
                userEmail = uiState.userEmail,
                infoMessage = uiState.infoMessage,
                onSignOutClick = {
                    authViewModel.signOut()
                    navController.navigate(AppRoute.LOGIN) {
                        popUpTo(AppRoute.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }

}