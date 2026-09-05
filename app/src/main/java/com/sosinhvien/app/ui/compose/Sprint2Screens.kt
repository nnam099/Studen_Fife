package com.sosinhvien.app.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sosinhvien.app.data.auth.AuthRepository
import com.sosinhvien.app.data.network.compose.AcademicTermResponse
import com.sosinhvien.app.data.network.compose.BudgetResponse
import com.sosinhvien.app.data.network.compose.CategoryResponse
import com.sosinhvien.app.data.network.compose.CreateBudgetRequest
import com.sosinhvien.app.data.network.compose.CreateMilestoneRequest
import com.sosinhvien.app.data.network.compose.CreateTransactionRequest
import com.sosinhvien.app.data.network.compose.MilestoneResponse
import com.sosinhvien.app.data.network.compose.ReportResponse
import com.sosinhvien.app.data.network.compose.TransactionResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.NumberFormat
import java.util.Locale

// --- ONBOARDING 3-STEP SCREEN ---
@Composable
fun OnboardingScreen(repository: AuthRepository, navController: NavHostController) {
    var step by remember { mutableIntStateOf(1) }
    var termName by remember { mutableStateOf("Học kỳ 1 (2026)") }
    var startDate by remember { mutableStateOf("2026-01-05") }
    var endDate by remember { mutableStateOf("2026-05-30") }
    var budgetAmount by remember { mutableStateOf("15000000") }
    var milestoneTitle by remember { mutableStateOf("Thi giữa kỳ môn CSDL") }
    var milestoneDueDate by remember { mutableStateOf("2026-03-20") }
    
    var createdTermId by remember { mutableStateOf<String?>(null) }
    var createdCategoryId by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    SprintShell(navController, "Thiết lập ban đầu", "onboarding") {
        // Step indicator bar
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StepChip("1. Học kỳ", active = step == 1, completed = step > 1)
            StepChip("2. Ngân sách", active = step == 2, completed = step > 2)
            StepChip("3. Deadline", active = step == 3, completed = step > 3)
        }
        Spacer(Modifier.height(12.dp))

        when (step) {
            1 -> {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Bước 1: Chọn / Tạo học kỳ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Tạo học kỳ mới để liên kết ngân sách và mốc thời gian thi cử của bạn.", style = MaterialTheme.typography.bodySmall)
                        FormField("Tên học kỳ", termName) { termName = it }
                        FormField("Ngày bắt đầu (YYYY-MM-DD)", startDate) { startDate = it }
                        FormField("Ngày kết thúc (YYYY-MM-DD)", endDate) { endDate = it }
                        Button(
                            onClick = {
                                scope.launch {
                                    loading = true
                                    try {
                                        val term = repository.createAcademicTerm(termName, startDate, endDate)
                                        createdTermId = term.id
                                        val cats = repository.categories()
                                        val cat = if (cats.isNotEmpty()) cats.first() else repository.createCategory("Chi tiêu chung")
                                        createdCategoryId = cat.id
                                        step = 2
                                        message = "Đã tạo học kỳ: ${term.name}"
                                    } catch (e: Exception) {
                                        message = errorMessage(e)
                                    } finally { loading = false }
                                }
                            },
                            enabled = !loading && termName.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (loading) "Đang lưu..." else "Tiếp tục sang Ngân sách →") }
                    }
                }
            }
            2 -> {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Bước 2: Ngân sách học kỳ cốt lõi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Đặt hạn mức ngân sách tổng hợp cho kỳ học này.", style = MaterialTheme.typography.bodySmall)
                        FormField("Số tiền ngân sách (VND)", budgetAmount) { budgetAmount = it }
                        Button(
                            onClick = {
                                scope.launch {
                                    loading = true
                                    try {
                                        val amt = budgetAmount.toDoubleOrNull() ?: 15000000.0
                                        repository.createBudget(
                                            CreateBudgetRequest(
                                                amount = amt,
                                                periodType = "academic_term",
                                                startDate = startDate,
                                                endDate = endDate,
                                                currency = "VND",
                                                academicTermId = createdTermId ?: "",
                                                categoryId = createdCategoryId ?: ""
                                            )
                                        )
                                        step = 3
                                        message = "Đã tạo ngân sách học kỳ"
                                    } catch (e: Exception) {
                                        message = errorMessage(e)
                                    } finally { loading = false }
                                }
                            },
                            enabled = !loading && budgetAmount.toDoubleOrNull() != null,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (loading) "Đang lưu..." else "Tiếp tục sang Mốc học kỳ →") }
                    }
                }
            }
            3 -> {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Bước 3: Mốc học kỳ quan trọng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Thêm các mốc thi cử, đồ án hoặc hạn nộp học phí đầu tiên.", style = MaterialTheme.typography.bodySmall)
                        FormField("Tên mốc quan trọng", milestoneTitle) { milestoneTitle = it }
                        FormField("Hạn hoàn thành (YYYY-MM-DD)", milestoneDueDate) { milestoneDueDate = it }
                        Button(
                            onClick = {
                                scope.launch {
                                    loading = true
                                    try {
                                        if (createdTermId != null && milestoneTitle.isNotBlank()) {
                                            repository.createMilestone(
                                                CreateMilestoneRequest(
                                                    title = milestoneTitle,
                                                    dueDate = milestoneDueDate,
                                                    type = "exam",
                                                    academicTermId = createdTermId!!
                                                )
                                            )
                                        }
                                        navController.navigate("home") {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    } catch (e: Exception) {
                                        message = errorMessage(e)
                                    } finally { loading = false }
                                }
                            },
                            enabled = !loading && milestoneTitle.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Hoàn thành Onboarding và đến Dashboard ✓") }
                    }
                }
            }
        }
        ResultText(message)
    }
}

// --- HOME / DASHBOARD SCREEN ---
@Composable
fun HomeScreen(repository: AuthRepository, navController: NavHostController) {
    var terms by remember { mutableStateOf<List<AcademicTermResponse>>(emptyList()) }
    var budgets by remember { mutableStateOf<List<BudgetResponse>>(emptyList()) }
    var transactions by remember { mutableStateOf<List<TransactionResponse>>(emptyList()) }
    var milestones by remember { mutableStateOf<List<MilestoneResponse>>(emptyList()) }
    var report by remember { mutableStateOf<ReportResponse?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        runCatching {
            terms = repository.academicTerms()
            budgets = repository.budgets()
            transactions = repository.transactions()
            milestones = repository.milestones()
            report = repository.report()
        }.onFailure { message = errorMessage(it) }
    }

    SprintShell(navController, "Home / Dashboard", "home") {
        if (terms.isEmpty()) {
            AlertCard(
                title = "Chưa thiết lập học kỳ!",
                description = "Bạn chưa có học kỳ nào. Hãy tạo học kỳ ở phần Onboarding để ghi nhận chi tiêu & deadline.",
                buttonText = "Tạo học kỳ ngay",
                onClick = { navController.navigate("onboarding") }
            )
        }

        // Summary Cards Grid
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryCard(
                title = "Tổng chi học kỳ",
                value = formatVnd(report?.totalExpenses ?: 0.0),
                subtitle = "${transactions.size} giao dịch",
                modifier = Modifier.weight(1f)
            )
            val budgetTotal = budgets.firstOrNull()?.amount?.toDoubleOrNull() ?: 0.0
            val remaining = (budgetTotal - (report?.totalExpenses ?: 0.0)).coerceAtLeast(0.0)
            SummaryCard(
                title = "Ngân sách còn lại",
                value = formatVnd(remaining),
                subtitle = "Hạn mức: ${formatVnd(budgetTotal)}",
                modifier = Modifier.weight(1f),
                isHighlight = remaining < budgetTotal * 0.2
            )
        }

        if (budgets.isNotEmpty()) {
            val totalSpent = report?.totalExpenses ?: 0.0
            val budgetAmount = budgets.first().amount.toDoubleOrNull() ?: 1.0
            val progress = (totalSpent / budgetAmount).toFloat().coerceIn(0f, 1f)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("Tỷ lệ sử dụng ngân sách", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = if (progress > 0.85f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("${(progress * 100).toInt()}% đã tiêu dùng", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Upcoming Deadlines Section
        Text("Deadline / Mốc sắp tới", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (milestones.isEmpty()) {
            Text("Chưa có mốc deadline nào.", style = MaterialTheme.typography.bodySmall)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(milestones.take(4)) { m ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(m.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Hạn: ${m.dueDate}", fontSize = 12.sp)
                            PriorityBadge(m.priority)
                        }
                    }
                }
            }
        }

        // Recent Transactions Section
        Text("Giao dịch gần đây", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (transactions.isEmpty()) {
            Text("Chưa có giao dịch nào.", style = MaterialTheme.typography.bodySmall)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                transactions.take(5).forEach { tx ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(tx.description, fontWeight = FontWeight.SemiBold)
                                Text(tx.occurredAt, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(formatVnd(tx.amount.toDoubleOrNull() ?: 0.0), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
        ResultText(message)
    }
}

// --- NGÂN SÁCH SCREEN (3 REQUIRED LABELS + TABS) ---
@Composable
fun BudgetScreen(repository: AuthRepository, navController: NavHostController) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Học kỳ, 1: Tháng, 2: Tuần
    var budgets by remember { mutableStateOf<List<BudgetResponse>>(emptyList()) }
    var terms by remember { mutableStateOf<List<AcademicTermResponse>>(emptyList()) }
    var categories by remember { mutableStateOf<List<CategoryResponse>>(emptyList()) }
    var report by remember { mutableStateOf<ReportResponse?>(null) }
    var amountInput by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        runCatching {
            budgets = repository.budgets()
            terms = repository.academicTerms()
            categories = repository.categories()
            report = repository.report()
            if (categories.isEmpty()) categories = listOf(repository.createCategory("Chi tiêu chung"))
        }.onFailure { message = errorMessage(it) }
    }

    SprintShell(navController, "Quản lý ngân sách", "budget") {
        // Tab header for period_type
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) { Text("Học kỳ", Modifier.padding(12.dp)) }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) { Text("Tháng", Modifier.padding(12.dp)) }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) { Text("Tuần", Modifier.padding(12.dp)) }
        }
        Spacer(Modifier.height(12.dp))

        // 3 EXACT REQUIRED LABELS DISPLAY: [Đã chi] / [Ngân sách] / [Còn lại]
        val totalSpent = report?.totalExpenses ?: 0.0
        val totalBudget = budgets.firstOrNull()?.amount?.toDoubleOrNull() ?: 0.0
        val remaining = totalBudget - totalSpent

        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tổng quan ngân sách (${if (selectedTab == 0) "Học kỳ" else if (selectedTab == 1) "Tháng" else "Tuần"})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    BudgetStatItem(label = "[Đã chi]", value = formatVnd(totalSpent), color = MaterialTheme.colorScheme.error)
                    BudgetStatItem(label = "[Ngân sách]", value = formatVnd(totalBudget), color = MaterialTheme.colorScheme.primary)
                    BudgetStatItem(label = "[Còn lại]", value = formatVnd(remaining), color = if (remaining >= 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error)
                }
            }
        }

        // Add/Update budget card
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Đặt hạn mức ngân sách mới", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                FormField("Số tiền hạn mức (VND)", amountInput) { amountInput = it }
                Button(
                    onClick = {
                        val amt = amountInput.toDoubleOrNull()
                        if (amt == null || amt <= 0) {
                            message = "Số tiền ngân sách phải là số dương hợp lệ!"
                            return@Button
                        }
                        if (terms.isEmpty()) {
                            message = "Vui lòng tạo học kỳ trước khi đặt ngân sách!"
                            return@Button
                        }
                        scope.launch {
                            try {
                                val period = when (selectedTab) { 1 -> "monthly"; 2 -> "weekly"; else -> "academic_term" }
                                repository.createBudget(
                                    CreateBudgetRequest(
                                        amount = amt,
                                        periodType = period,
                                        startDate = terms.first().startDate,
                                        endDate = terms.first().endDate,
                                        currency = "VND",
                                        academicTermId = terms.first().id,
                                        categoryId = categories.first().id
                                    )
                                )
                                budgets = repository.budgets()
                                amountInput = ""
                                message = "Đã lưu ngân sách thành công!"
                            } catch (e: Exception) { message = errorMessage(e) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Lưu hạn mức") }
            }
        }
        ResultText(message)
    }
}

// --- THÊM CHI TIÊU SCREEN ---
@Composable
fun TransactionScreen(repository: AuthRepository, navController: NavHostController) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var occurredAt by remember { mutableStateOf("2026-02-10") }
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
            if (categories.isEmpty()) categories = listOf(repository.createCategory("Ăn uống"))
        }.onFailure { message = errorMessage(it) }
    }

    SprintShell(navController, "Thêm chi tiêu", "add-expense") {
        // EDGE CASE: If no terms created yet, prompt user
        if (terms.isEmpty()) {
            AlertCard(
                title = "Chưa có học kỳ!",
                description = "Bạn chưa thể thêm giao dịch vì chưa khởi tạo học kỳ nào. Hãy tạo học kỳ ở Onboarding.",
                buttonText = "Đi đến Onboarding",
                onClick = { navController.navigate("onboarding") }
            )
        } else {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Tạo giao dịch chi tiêu mới", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    FormField("Số tiền (VND)", amount) { amount = it }
                    FormField("Mô tả khoản chi", description) { description = it }
                    FormField("Ngày phát sinh (YYYY-MM-DD)", occurredAt) { occurredAt = it }
                    
                    Button(
                        onClick = {
                            val valAmt = amount.toDoubleOrNull()
                            if (valAmt == null || valAmt <= 0) {
                                message = "Số tiền phải là số dương hợp lệ!"
                                return@Button
                            }
                            if (description.isBlank()) {
                                message = "Vui lòng nhập mô tả chi tiêu!"
                                return@Button
                            }
                            scope.launch {
                                try {
                                    repository.createTransaction(
                                        CreateTransactionRequest(
                                            amount = valAmt,
                                            type = "expense",
                                            description = description,
                                            occurredAt = occurredAt,
                                            categoryId = categories.first().id,
                                            academicTermId = terms.first().id
                                        )
                                    )
                                    transactions = repository.transactions()
                                    amount = ""
                                    description = ""
                                    message = "Đã ghi nhận chi tiêu!"
                                } catch (e: Exception) { message = errorMessage(e) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Lưu giao dịch") }
                }
            }
        }
        ResultText(message)

        Spacer(Modifier.height(12.dp))
        Text("Lịch sử giao dịch", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(transactions) { tx ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(tx.description, fontWeight = FontWeight.SemiBold)
                            Text(tx.occurredAt, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(formatVnd(tx.amount.toDoubleOrNull() ?: 0.0), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- DEADLINE / MILESTONE SCREEN ---
@Composable
fun MilestoneScreen(repository: AuthRepository, navController: NavHostController) {
    var title by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("2026-03-15") }
    var type by remember { mutableStateOf("exam") }
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

    SprintShell(navController, "Deadline & Mốc học kỳ", "deadline") {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Tạo deadline / mốc quan trọng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                FormField("Tên deadline", title) { title = it }
                FormField("Hạn chót (YYYY-MM-DD)", dueDate) { dueDate = it }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = type == "exam", onClick = { type = "exam" }, label = { Text("Thi cử") })
                    FilterChip(selected = type == "assignment", onClick = { type = "assignment" }, label = { Text("Bài tập") })
                    FilterChip(selected = type == "project", onClick = { type = "project" }, label = { Text("Đồ án") })
                }
                Button(
                    onClick = {
                        if (terms.isEmpty()) {
                            message = "Vui lòng tạo học kỳ trước khi tạo deadline!"
                            return@Button
                        }
                        scope.launch {
                            try {
                                repository.createMilestone(
                                    CreateMilestoneRequest(
                                        title = title,
                                        dueDate = dueDate,
                                        type = type,
                                        priority = 2,
                                        academicTermId = terms.first().id
                                    )
                                )
                                milestones = repository.milestones()
                                title = ""
                                message = "Đã tạo mốc deadline!"
                            } catch (e: Exception) { message = errorMessage(e) }
                        }
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Thêm deadline") }
            }
        }
        ResultText(message)

        Spacer(Modifier.height(12.dp))
        Text("Danh sách deadline", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(milestones) { m ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(m.title, fontWeight = FontWeight.Bold)
                            Text("Hạn: ${m.dueDate} · ${m.type}", style = MaterialTheme.typography.bodySmall)
                        }
                        PriorityBadge(m.priority)
                    }
                }
            }
        }
    }
}

// --- BÁO CÁO SCREEN (NEW) ---
@Composable
fun ReportScreen(repository: AuthRepository, navController: NavHostController) {
    var report by remember { mutableStateOf<ReportResponse?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        runCatching { report = repository.report() }.onFailure { message = errorMessage(it) }
    }

    SprintShell(navController, "Báo cáo chi tiêu", "report") {
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Tổng quan báo cáo tài chính", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ReportStatItem("Tổng chi học kỳ", formatVnd(report?.totalExpenses ?: 0.0))
                    ReportStatItem("Chi trong tuần", formatVnd(report?.weeklyExpenses ?: 0.0))
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Phân tích danh mục chi tiêu cao nhất", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        val topCats = report?.topCategories ?: emptyList()
        if (topCats.isEmpty()) {
            Text("Chưa có dữ liệu danh mục.", style = MaterialTheme.typography.bodySmall)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                topCats.forEach { cat ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(cat.categoryName, fontWeight = FontWeight.SemiBold)
                                Text("${formatVnd(cat.totalAmount)} (${cat.percentage}%)")
                            }
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (cat.percentage / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp)
                            )
                        }
                    }
                }
            }
        }
        ResultText(message)
    }
}

// --- CÀI ĐẶT SCREEN (NEW) ---
@Composable
fun SettingsScreen(repository: AuthRepository, navController: NavHostController) {
    var notificationsEnabled by remember { mutableStateOf(true) }

    SprintShell(navController, "Cài đặt & Tài khoản", "settings") {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Thông tin sinh viên", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Tài khoản: Sinh viên PTIT")
                Text("Trạng thái: Đã xác thực JWT")
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Cấu hình ứng dụng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Thông báo vượt ngân sách & deadline")
                    Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tài khoản & Bảo mật", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Button(
                    onClick = {
                        repository.logout()
                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Đăng xuất tài khoản") }
            }
        }
    }
}

// --- HELPER COMPOSABLES & UTILS ---
@Composable
private fun StepChip(text: String, active: Boolean, completed: Boolean) {
    Surface(
        color = if (completed) MaterialTheme.colorScheme.primaryContainer else if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun SummaryCard(title: String, value: String, subtitle: String, modifier: Modifier = Modifier, isHighlight: Boolean = false) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = if (isHighlight) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun BudgetStatItem(label: String, value: String, color: Color) {
    Column {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ReportStatItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun PriorityBadge(priority: Int) {
    val (text, color) = when (priority) {
        3 -> "Cao" to MaterialTheme.colorScheme.error
        2 -> "Trung bình" to MaterialTheme.colorScheme.tertiary
        else -> "Thấp" to MaterialTheme.colorScheme.secondary
    }
    Surface(color = color.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
        Text(text, color = color, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
    }
}

@Composable
private fun AlertCard(title: String, description: String, buttonText: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
            Button(onClick = onClick) { Text(buttonText) }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SprintShell(navController: NavHostController, title: String, selectedRoute: String, content: @Composable () -> Unit) {
    val destinations = listOf(
        "onboarding" to "Onboarding",
        "home" to "Home",
        "add-expense" to "Chi tiêu",
        "budget" to "Budget",
        "deadline" to "Deadline",
        "report" to "Báo cáo",
        "settings" to "Cài đặt"
    )
    Scaffold(
        topBar = { TopAppBar(title = { Text(title, fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) },
        bottomBar = {
            NavigationBar {
                destinations.forEach { (route, label) ->
                    NavigationBarItem(
                        selected = route == selectedRoute,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo("home")
                                launchSingleTop = true
                            }
                        },
                        icon = { Text(label.take(1)) },
                        label = { Text(label, fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun FormField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value, onValueChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
}

@Composable
private fun ResultText(message: String?) {
    message?.let { Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium) }
}

private fun errorMessage(error: Throwable): String = when (error) {
    is HttpException -> "Server error ${error.code()}"
    else -> error.message ?: "Không thể kết nối tới server"
}

private fun formatVnd(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(amount)
}
