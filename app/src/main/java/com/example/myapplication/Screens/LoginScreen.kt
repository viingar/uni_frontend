package com.example.myapplication.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.ViewModels.LoginViewModel
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.SpanStyle
import com.example.myapplication.api.RetrofitClient

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.LoginViewModelFactory(
            RetrofitClient.apiService,
            LocalContext.current
        )
    )
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val isFormValid = username.isNotBlank() && password.isNotBlank()

    val viewModel: LoginViewModel = viewModel()
    val loginSuccess by viewModel.loginSuccess.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            navController.navigate("main") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Username field
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Логин") },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = "Логин")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password field with toggle visibility
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "Пароль")
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(
            onClick = { navController.navigate("forgot_password") }
        ) {
            Text("Забыли пароль?")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isFormValid) {
                    viewModel.loginUser(username, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid
        ) {
            Text("Войти")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ссылка для перехода к регистрации
        ClickableText(
            text = buildAnnotatedString {
                append("Нет аккаунта? ")
                withStyle(style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )) {
                    append("Зарегистрироваться")
                }
            },
            onClick = { navController.navigate("register") }
        )
    }
}