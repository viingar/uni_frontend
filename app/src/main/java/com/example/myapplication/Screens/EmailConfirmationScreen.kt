package com.example.myapplication.Screens
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.ViewModels.EmailConfirmationViewModel
import androidx.lifecycle.ViewModelProvider

@Composable
fun EmailConfirmationScreen(
    navController: NavController,
    email: String,
    viewModel: EmailConfirmationViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EmailConfirmationViewModel(email) as T
            }
        }
    )
) {
    var verificationCode by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val verificationSuccess by viewModel.verificationSuccess.collectAsState()

    LaunchedEffect(verificationSuccess) {
        if (verificationSuccess) {
            navController.navigate("login") {
                popUpTo(0)
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
        OutlinedTextField(
            value = email,
            onValueChange = {},
            label = { Text("Email") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = verificationCode,
            onValueChange = { verificationCode = it },
            label = { Text("Verification Code") },
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (verificationCode.isNotBlank()) {
                    viewModel.verifyEmail(verificationCode)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = verificationCode.isNotBlank() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Verify Email")
            }
        }
    }
}