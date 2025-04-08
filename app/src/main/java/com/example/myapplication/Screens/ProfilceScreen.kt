package com.example.myapplication.Screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.ViewModels.MainViewModel
import com.example.myapplication.ViewModels.MainViewModel.MainViewModelFactory
import com.example.myapplication.ViewModels.RegisterViewModel
import com.example.myapplication.api.Receipt
import com.example.myapplication.api.ReceiptData
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.myapplication.R
import com.example.myapplication.ViewModels.ProfileViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.ProfileViewModelFactory(LocalContext.current)
    )
) {
    val userInfo by viewModel.userInfo.collectAsState()
    val showErrorDialog = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf("") }
    val logoutSuccess by viewModel.logoutSuccess.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUserInfo()
    }

    LaunchedEffect(logoutSuccess) {
        if (logoutSuccess) {
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
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
        // Поле для email
        Text(
            text = "Email: ${userInfo?.email ?: "Loading..."}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(8.dp)
        )

        // Поле для username
        Text(
            text = "Username: ${userInfo?.username ?: "Loading..."}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка выхода
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text("Logout")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка отправки ошибки
        Button(
            onClick = { showErrorDialog.value = true }
        ) {
            Text("Report an issue")
        }
    }

    // Диалоговое окно для отправки ошибки
    if (showErrorDialog.value) {
        AlertDialog(
            onDismissRequest = { showErrorDialog.value = false },
            title = { Text("Report an issue") },
            text = {
                Column {
                    TextField(
                        value = errorMessage.value,
                        onValueChange = { errorMessage.value = it },
                        label = { Text("Describe your issue") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reportError(errorMessage.value)
                        showErrorDialog.value = false
                    }
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                IconButton(
                    onClick = { showErrorDialog.value = false }
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        )
    }
}