package com.sosinhvien.app.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.sosinhvien.app.data.auth.AuthRepository
import com.sosinhvien.app.data.network.compose.AcademicTermResponse
import com.sosinhvien.app.data.network.compose.BudgetResponse
import com.sosinhvien.app.data.network.compose.CategoryResponse
import com.sosinhvien.app.data.network.compose.CreateBudgetRequest
import com.sosinhvien.app.data.network.compose.CreateMilestoneRequest
import com.sosinhvien.app.data.network.compose.CreateTransactionRequest
import com.sosinhvien.app.data.network.compose.MilestoneResponse
import com.sosinhvien.app.data.network.compose.TransactionResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun AcademicTermScreen(repository: AuthRepository, navController: NavHostController) {
    var name by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("2026-01-05") }
    var endDate by remember { mutableStateOf("2026-05-30") }
    var terms by remember { mutableStateOf<List<AcademicTermResponse>>(emptyList()) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { terms = runCatching { repository.academicTerms() }.getOrDefault(emptyList()) }

    Sprint2Shell(navController, "Setup học kỳ", "onboarding") {
        Text("Tạo học kỳ để liên kết ngân sách, giao dịch và deadline.")
        FormField("Tên học kỳ", name) { name = it }
        FormField("Ngày bắt đầu (YYYY-MM-DD)", startDate) { startDate = it }
        FormField("Ngày kết thúc (YYYY-MM-DD)", endDate) { endDate = it }
        ActionButton("Tạo học kỳ", enabled = name.isNotBlank()) {
            scope.launch {
                message = runCatching {
                    repository.createAcademicTerm(name, startDate, endDate)
                    terms = repository.academicTerms()
                    name = ""
                    "Đã tạo học kỳ"
                }.getOrElse(::errorMessage)
            }
        }
        ResultText(message)
        Spacer(Modifier.height(16.dp))
        Text("Học kỳ của bạn", style = MaterialTheme.typography.titleMedium)
        terms.forEach { Text("${it.name} · ${it.startDate} → ${it.endDate}") }
    }
}

@Composable
fun BudgetScreen(repository: AuthRepository, navController: NavHostController) {
    var amount by remember { mutableStateOf("") }
    var budgets by remember { mutableStateOf<List<BudgetResponse>>(emptyList()) }
    var terms by remember { mutableStateOf<List<AcademicTermResponse>>(emptyList()) }
    var categories by remember { mutableStateOf<List<CategoryResponse>>(emptyList()) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        runCatching {
            budgets = repository.budgets()
            terms = repository.academicTerms()
            categories = repository.categories()
            if (categories.isEmpty()) categories = listOf(repository.createCategory("Chi tiêu chung"))
        }.onFailure { message = errorMessage(it) }
    }

    Sprint2Shell(navController, "Ngân sách", "budget") {
        Text("${terms.firstOrNull()?.name ?: "Chưa có học kỳ"} · ${categories.firstOrNull()?.name ?: "Chưa có danh mục"}")
        FormField("Số tiền VND", amount) { amount = it }
        ActionButton("Tạo ngân sách", enabled = amount.toDoubleOrNull() != null && terms.isNotEmpty() && categories.isNotEmpty()) {
            scope.launch {
                message = runCatching {
                    repository.createBudget(CreateBudgetRequest(amount.toDouble(), "monthly", terms.first().startDate, terms.first().endDate, "VND", terms.first().id, categories.first().id))
                    budgets = repository.budgets()
                    amount = ""
                    "Đã tạo ngân sách"
                }.getOrElse(::errorMessage)
            }
        }
        ResultText(message)
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(budgets) { budget ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${budget.amount} ${budget.currency} · ${budget.periodType}")
                    OutlinedButton(onClick = {
                        scope.launch {
                            message = runCatching {
                                repository.updateBudget(budget.id, budget.amount.toDouble() + 100000.0, budget.periodType)
                                budgets = repository.budgets()
                                "Đã cập nhật ngân sách"
                            }.getOrElse(::errorMessage)
                        }
                    }) { Text("Sửa +100k") }
                }
            }
        }
    }
}

@Composable
fun TransactionScreen(repository: AuthRepository, navController: NavHostController) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var transactions by remember { mutableStateOf<List<TransactionResponse>>(emptyList()) }
    var terms by remember { mutableStateOf<List<AcademicTermResponse>>(emptyList()) }
    var categories by remember { mutableStateOf<List<CategoryResponse>>(emptyList()) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        runCatching {
            transactions = repository.transactions()
            terms = repository.academicTerms()
            categories = repository.categories()
        }.onFailure { message = errorMessage(it) }
    }

    Sprint2Shell(navController, "Thêm chi tiêu", "add-expense") {
        Text("Ghi nhận giao dịch trong học kỳ hiện tại.")
        FormField("Số tiền VND", amount) { amount = it }
        FormField("Mô tả", description) { description = it }
        ActionButton("Lưu giao dịch", enabled = amount.toDoubleOrNull() != null && description.isNotBlank() && terms.isNotEmpty() && categories.isNotEmpty()) {
            scope.launch {
                message = runCatching {
                    repository.createTransaction(CreateTransactionRequest(amount.toDouble(), "expense", description, "2026-02-10", categories.first().id, terms.first().id))
                    transactions = repository.transactions()
                    amount = ""
                    description = ""
                    "Đã lưu giao dịch"
                }.getOrElse(::errorMessage)
            }
        }
        ResultText(message)
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(transactions) { transaction -> Text("${transaction.amount} VND · ${transaction.description} · ${transaction.occurredAt}") }
        }
    }
}

@Composable
fun MilestoneScreen(repository: AuthRepository, navController: NavHostController) {
    var title by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("2026-03-15") }
    var milestones by remember { mutableStateOf<List<MilestoneResponse>>(emptyList()) }
    var terms by remember { mutableStateOf<List<AcademicTermResponse>>(emptyList()) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        runCatching {
            milestones = repository.milestones()
            terms = repository.academicTerms()
        }.onFailure { message = errorMessage(it) }
    }

    Sprint2Shell(navController, "Deadline / Mốc", "deadline") {
        FormField("Tên mốc", title) { title = it }
        FormField("Hạn (YYYY-MM-DD)", dueDate) { dueDate = it }
        ActionButton("Tạo deadline", enabled = title.isNotBlank() && terms.isNotEmpty()) {
            scope.launch {
                message = runCatching {
                    repository.createMilestone(CreateMilestoneRequest(title, dueDate = dueDate, type = "personal_goal", academicTermId = terms.first().id))
                    milestones = repository.milestones()
                    title = ""
                    "Đã tạo deadline"
                }.getOrElse(::errorMessage)
            }
        }
        ResultText(message)
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(milestones) { milestone -> Text("${milestone.title} · ${milestone.dueDate} · ${milestone.type}") }
        }
    }
}

@Composable
private fun Sprint2Shell(navController: NavHostController, title: String, selectedRoute: String, content: @Composable () -> Unit) {
    val destinations = listOf("onboarding" to "Kỳ học", "home" to "Home", "add-expense" to "Chi", "budget" to "Budget", "deadline" to "Mốc", "report" to "Báo cáo", "settings" to "Cài đặt")
    Scaffold(bottomBar = {
        NavigationBar {
            destinations.forEach { (route, label) ->
                NavigationBarItem(selected = route == selectedRoute, onClick = { navController.navigate(route) }, icon = { Text(label.take(1)) }, label = { Text(label) })
            }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            content()
        }
    }
}

@Composable
private fun FormField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value, onValueChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
}

@Composable
private fun ActionButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) { Text(label) }
}

@Composable
private fun ResultText(message: String?) {
    message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
}

private fun errorMessage(error: Throwable): String = when (error) {
    is HttpException -> "Server error ${error.code()}"
    else -> error.message ?: "Không thể kết nối tới server"
}
