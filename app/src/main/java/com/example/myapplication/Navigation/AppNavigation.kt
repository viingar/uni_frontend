package com.example.myapplication.Navigation

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.Screens.AdminScreen
import com.example.myapplication.Screens.EmailConfirmationScreen
import com.example.myapplication.Screens.LoginScreen
import com.example.myapplication.Screens.MainScreen
import com.example.myapplication.Screens.ProfileScreen
import com.example.myapplication.Screens.RegisterScreen
import com.example.myapplication.Screens.ResetPasswordScreen
import com.example.myapplication.Screens.SendEmailScreen
import com.example.myapplication.Screens.ValidationCodeScreen
import com.example.myapplication.Screens.YandexMapWithLocation

@OptIn(ExperimentalGetImage::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "register"
    ) {
        composable("register") {
            RegisterScreen(navController)
        }
        composable(
            "email_confirmation/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            EmailConfirmationScreen(
                navController = navController,
                email = email
            )
        }
        composable("admin") {
            AdminScreen(navController)
        }
        composable("map") {
            YandexMapWithLocation(navController)
        }
        composable("login"){
            LoginScreen(navController)
        }
        composable("profile"){
            ProfileScreen(navController)
        }
        composable("main"){
            MainScreen(navController)
        }
        composable("forgot_password") { SendEmailScreen(navController) }
        composable(
            "validation_code/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType } )
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                ValidationCodeScreen(navController, email)
            }
        composable(
            "reset_password/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType } )
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                ResetPasswordScreen(navController, email)
            }
    }
}