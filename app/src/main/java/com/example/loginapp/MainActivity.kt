package com.example.loginapp

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.loginapp.auth.AuthViewModel
import com.example.loginapp.screens.BookingCinemaScreen
import com.example.loginapp.screens.BookingHistoryScreen
import com.example.loginapp.screens.BookingSeatScreen
import com.example.loginapp.screens.BookingTicketScreen
import com.example.loginapp.screens.ForgotPasswordScreen
import com.example.loginapp.screens.LoginScreen
import com.example.loginapp.screens.MainScreen
import com.example.loginapp.screens.MovieDetailScreen
import com.example.loginapp.screens.SignUpScreen
import com.example.loginapp.ui.theme.LoginAppTheme

// Khai báo các route dùng để điều hướng giữa các màn hình
private object AppRoute {
    const val LOGIN         = "login"
    const val SIGN_UP       = "sign_up"
    const val FORGOT_PASSWORD = "forgot_password"
    const val MAIN          = "main"                // Màn hình chính có bottom nav
    const val MOVIE_DETAIL  = "movie_detail"        // Màn hình chi tiết phim
    const val BOOKING_CINEMA   = "booking_cinema"
    const val BOOKING_SEAT     = "booking_seat"
    const val BOOKING_TICKET   = "booking_ticket"
    const val BOOKING_HISTORY  = "booking_history"
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
        startDestination = if (uiState.isAuthenticated) AppRoute.MAIN else AppRoute.LOGIN
    ) {
        // --- Màn hình đăng nhập ---
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
                        navController.navigate(AppRoute.MAIN) {
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

        // --- Màn hình đăng ký ---
        composable(AppRoute.SIGN_UP) {
            SignUpScreen(
                onBackClick = {
                    authViewModel.clearFeedback()
                    navController.popBackStack()
                },
                onSignUpClick = { fullName, email, password, confirmPassword ->
                    authViewModel.signUp(fullName, email, password, confirmPassword) {
                        navController.navigate(AppRoute.MAIN) {
                            popUpTo(AppRoute.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage
            )
        }

        // --- Màn hình quên mật khẩu ---
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

        // --- Màn hình chính (Home / Phim / Hồ sơ) — có bottom navigation ---
        composable(AppRoute.MAIN) {
            MainScreen(
                authViewModel = authViewModel,
                onMovieClick = { movieId ->
                    navController.navigate("${AppRoute.MOVIE_DETAIL}/${Uri.encode(movieId)}")
                },
                onCinemaShowtimeClick = { movieId, showtimeId ->
                    navController.navigate("${AppRoute.BOOKING_SEAT}/${Uri.encode(movieId)}/${Uri.encode(showtimeId)}")
                },
                onSignOutClick = {
                    authViewModel.signOut()
                    navController.navigate(AppRoute.LOGIN) {
                        popUpTo(AppRoute.MAIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onHistoryClick = {
                    navController.navigate(AppRoute.BOOKING_HISTORY)
                }
            )
        }

        // --- Màn hình lịch sử đặt vé ---
        composable(AppRoute.BOOKING_HISTORY) {
            BookingHistoryScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- Màn hình chi tiết phim ---
        composable(
            route = "${AppRoute.MOVIE_DETAIL}/{movieId}",
            arguments = listOf(navArgument("movieId") { type = NavType.StringType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId").orEmpty()
            MovieDetailScreen(
                movieId = movieId,
                onBackClick = { navController.popBackStack() },
                onBookClick = {
                    navController.navigate("${AppRoute.BOOKING_CINEMA}/${Uri.encode(movieId)}")
                }
            )
        }

        composable(
            route = "${AppRoute.BOOKING_CINEMA}/{movieId}",
            arguments = listOf(navArgument("movieId") { type = NavType.StringType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId").orEmpty()
            BookingCinemaScreen(
                movieId = movieId,
                onBackClick = { navController.popBackStack() },
                onShowtimeClick = { showtimeId ->
                    navController.navigate("${AppRoute.BOOKING_SEAT}/${Uri.encode(movieId)}/${Uri.encode(showtimeId)}")
                }
            )
        }

        composable(
            route = "${AppRoute.BOOKING_SEAT}/{movieId}/{showtimeId}",
            arguments = listOf(
                navArgument("movieId") { type = NavType.StringType },
                navArgument("showtimeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId").orEmpty()
            val showtimeId = backStackEntry.arguments?.getString("showtimeId").orEmpty()
            BookingSeatScreen(
                showtimeId = showtimeId,
                onBackClick = { navController.popBackStack() },
                onContinueClick = { seats ->
                    navController.navigate(
                        "${AppRoute.BOOKING_TICKET}/${Uri.encode(movieId)}/${Uri.encode(showtimeId)}/${Uri.encode(seats)}"
                    )
                }
            )
        }

        composable(
            route = "${AppRoute.BOOKING_TICKET}/{movieId}/{showtimeId}/{seats}",
            arguments = listOf(
                navArgument("movieId") { type = NavType.StringType },
                navArgument("showtimeId") { type = NavType.StringType },
                navArgument("seats") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            BookingTicketScreen(
                movieId = backStackEntry.arguments?.getString("movieId").orEmpty(),
                showtimeId = backStackEntry.arguments?.getString("showtimeId").orEmpty(),
                seatsText = backStackEntry.arguments?.getString("seats").orEmpty(),
                onBackClick = { navController.popBackStack() },
                onDoneClick = {
                    navController.navigate(AppRoute.MAIN) {
                        popUpTo(AppRoute.MAIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }

}
