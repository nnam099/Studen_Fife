package com.sosinhvien.app.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sosinhvien.app.data.auth.AuthRepository
import com.sosinhvien.app.data.auth.TokenStore
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                StudentFinanceApp()
            }
        }
    }
}

private object Routes {
    const val AUTH = "auth"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val ADD_EXPENSE = "add-expense"
    const val BUDGET = "budget"
    const val DEADLINE = "deadline"
    const val REPORT = "report"
    const val SETTINGS = "settings"
}

@Composable
private fun StudentFinanceApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { AuthRepository(TokenStore(context)) }
    val navController = rememberNavController()
    val startDest = if (repository.hasToken()) Routes.HOME else Routes.AUTH

    NavHost(navController = navController, startDestination = startDest) {
        composable(Routes.AUTH) {
            AuthScreen(
                repository = repository,
                onAuthenticated = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.ONBOARDING) { OnboardingScreen(repository, navController) }
        composable(Routes.HOME) { HomeScreen(repository, navController) }
        composable(Routes.ADD_EXPENSE) { TransactionScreen(repository, navController) }
        composable(Routes.BUDGET) { BudgetScreen(repository, navController) }
        composable(Routes.DEADLINE) { MilestoneScreen(repository, navController) }
        composable(Routes.REPORT) { ReportScreen(repository, navController) }
        composable(Routes.SETTINGS) { SettingsScreen(repository, navController) }
    }
}

@Composable
private fun AuthScreen(
    repository: AuthRepository,
    onAuthenticated: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var registerMode by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Quản lý Chi tiêu & Thời gian", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Dành riêng cho sinh viên Việt Nam MVP", style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(20.dp))

        Text(if (registerMode) "Tạo tài khoản mới" else "Đăng nhập hệ thống", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        if (registerMode) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Họ và tên sinh viên") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
        }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email sinh viên") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
        )
        Spacer(Modifier.height(16.dp))

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                scope.launch {
                    loading = true
                    errorMessage = null
                    try {
                        if (registerMode) {
                            repository.register(email, password, fullName)
                        } else {
                            repository.login(email, password)
                        }
                        onAuthenticated()
                    } catch (error: Exception) {
                        errorMessage = authError(error)
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = !loading && email.isNotBlank() && password.isNotBlank() &&
                (!registerMode || fullName.isNotBlank()),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (loading) "Đang kết nối..." else if (registerMode) "Đăng ký tài khoản" else "Đăng nhập")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                registerMode = !registerMode
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (registerMode) "Đã có tài khoản? Đăng nhập" else "Chưa có tài khoản? Đăng ký ngay")
        }
    }
}

private fun authError(error: Exception): String = when (error) {
    is HttpException -> "Lỗi phản hồi máy chủ: ${error.code()}"
    else -> error.message ?: "Không thể kết nối tới server backend"
}
