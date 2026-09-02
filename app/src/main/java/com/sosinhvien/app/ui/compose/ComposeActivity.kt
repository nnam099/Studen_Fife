package com.sosinhvien.app.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

    NavHost(navController = navController, startDestination = Routes.AUTH) {
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
        composable(Routes.ONBOARDING) { PlaceholderScreen(navController, "Onboarding") }
        composable(Routes.HOME) { HomeScreen(repository, navController) }
        composable(Routes.ADD_EXPENSE) { PlaceholderScreen(navController, "Thêm chi tiêu") }
        composable(Routes.BUDGET) { PlaceholderScreen(navController, "Ngân sách") }
        composable(Routes.DEADLINE) { PlaceholderScreen(navController, "Deadline") }
        composable(Routes.REPORT) { PlaceholderScreen(navController, "Báo cáo") }
        composable(Routes.SETTINGS) { PlaceholderScreen(navController, "Cài đặt") }
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
        Text("Student Finance", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(if (registerMode) "Tạo tài khoản" else "Đăng nhập")
        Spacer(Modifier.height(24.dp))

        if (registerMode) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Họ và tên") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
        }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
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
            Text(it, color = MaterialTheme.colorScheme.error)
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
            Text(if (loading) "Đang xử lý..." else if (registerMode) "Đăng ký" else "Đăng nhập")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                registerMode = !registerMode
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (registerMode) "Đã có tài khoản? Đăng nhập" else "Chưa có tài khoản? Đăng ký")
        }
    }
}

@Composable
private fun HomeScreen(repository: AuthRepository, navController: NavHostController) {
    val scope = rememberCoroutineScope()
    var refreshMessage by remember { mutableStateOf<String?>(null) }
    ShellScreen(navController, Routes.HOME, "Home / Dashboard") {
        Text("Coming soon")
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            scope.launch {
                refreshMessage = try {
                    repository.refresh()
                    "Session refreshed"
                } catch (error: Exception) {
                    authError(error)
                }
            }
        }) {
            Text("Refresh session")
        }
        refreshMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it)
        }
    }
}

@Composable
private fun PlaceholderScreen(
    navController: NavHostController,
    title: String,
) {
    ShellScreen(navController, routeForTitle(title), title) {
        Text("Coming soon")
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ShellScreen(
    navController: NavHostController,
    selectedRoute: String,
    title: String,
    content: @Composable () -> Unit,
) {
    val destinations = listOf(
        Routes.ONBOARDING to "Onboarding",
        Routes.HOME to "Home",
        Routes.ADD_EXPENSE to "Chi tiêu",
        Routes.BUDGET to "Ngân sách",
        Routes.DEADLINE to "Deadline",
        Routes.REPORT to "Báo cáo",
        Routes.SETTINGS to "Cài đặt",
    )
    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) },
        bottomBar = {
            NavigationBar {
                destinations.forEach { (route, label) ->
                    NavigationBarItem(
                        selected = selectedRoute == route,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(Routes.HOME)
                                launchSingleTop = true
                            }
                        },
                        icon = { Text(label.take(1)) },
                        label = { Text(label) },
                    )
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
        ) {
            content()
        }
    }
}

private fun routeForTitle(title: String): String = when (title) {
    "Onboarding" -> Routes.ONBOARDING
    "Thêm chi tiêu" -> Routes.ADD_EXPENSE
    "Ngân sách" -> Routes.BUDGET
    "Deadline" -> Routes.DEADLINE
    "Báo cáo" -> Routes.REPORT
    "Cài đặt" -> Routes.SETTINGS
    else -> Routes.HOME
}

private fun authError(error: Exception): String = when (error) {
    is HttpException -> "Server error ${error.code()}"
    else -> error.message ?: "Không thể kết nối tới server"
}
