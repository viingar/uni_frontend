package com.example.myapplication.Screens
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.Close
import com.example.myapplication.ViewModels.ProfileViewModel

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

        Text(
            text = "Email: ${userInfo?.email ?: "Loading..."}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(8.dp)
        )

        Text(
            text = "Username: ${userInfo?.username ?: "Loading..."}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Button(
            onClick = { showErrorDialog.value = true }
        ) {
            Text("Report an issue")
        }
    }

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