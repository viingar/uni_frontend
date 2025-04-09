package com.example.myapplication.Screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.myapplication.ViewModels.PasswordResetViewModel
import com.example.myapplication.ViewModels.PasswordResetViewModel.PasswordResetViewModelFactory
import com.example.myapplication.api.RetrofitClient

@Composable
fun ValidationCodeScreen(
    navController: NavController,
    email: String,
    viewModel: PasswordResetViewModel = viewModel(
        factory = PasswordResetViewModelFactory(
            RetrofitClient.apiService
        )
    )
) {
    var code by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enter verification code sent to $email",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Verification Code") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.validateCode(email, code) { success ->
                    if (success) {
                        navController.navigate("reset_password/$email")
                    } else {
                        Toast.makeText(
                            context,
                            "Invalid code",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = code.length == 6
        ) {
            Text("Verify Code")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { viewModel.resendCode(email) }
        ) {
            Text("Resend code")
        }
    }
}