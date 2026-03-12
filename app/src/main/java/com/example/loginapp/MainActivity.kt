package com.example.loginapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.loginapp.screens.ForgotPasswordScreen
import com.example.loginapp.screens.LoginScreen
import com.example.loginapp.screens.SignUpScreen
import com.example.loginapp.ui.theme.LoginAppTheme

// Khai báo các route dùng để điều hướng giữa các màn hình
private object AppRoute {
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val FORGOT_PASSWORD = "forgot_password"
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
fun AppNavigation() {
    // Tạo bộ điều hướng trung tâm cho ứng dụng
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoute.LOGIN
    ) {
        composable(AppRoute.LOGIN) {
            LoginScreen(
                onSignUpClick = {
                    navController.navigate(AppRoute.SIGN_UP)
                },
                onForgotPasswordClick = {
                    navController.navigate(AppRoute.FORGOT_PASSWORD)
                }
            )
        }

        composable(AppRoute.SIGN_UP) {
            SignUpScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoute.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }

}