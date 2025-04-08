package com.example.myapplication.Screens
import android.util.Patterns
import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.SpanStyle
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
            RetrofitClient.apiService // Ваш экземпляр ApiService
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
            enabled = code.length == 6 // Предполагаем 6-значный код
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