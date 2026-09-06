package com.sosinhvien.app.data.network.compose

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.sosinhvien.app.data.auth.TokenStore

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): AuthResponse

    @GET("academic-terms")
    suspend fun academicTerms(): List<AcademicTermResponse>

    @POST("academic-terms")
    suspend fun createAcademicTerm(@Body request: CreateAcademicTermRequest): AcademicTermResponse

    @GET("categories")
    suspend fun categories(): List<CategoryResponse>

    @POST("categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): CategoryResponse

    @GET("budgets")
    suspend fun budgets(): List<BudgetResponse>

    @POST("budgets")
    suspend fun createBudget(@Body request: CreateBudgetRequest): BudgetResponse

    @PATCH("budgets/{id}")
    suspend fun updateBudget(@Path("id") id: String, @Body request: UpdateBudgetRequest): BudgetResponse

    @GET("transactions")
    suspend fun transactions(): List<TransactionResponse>

    @POST("transactions")
    suspend fun createTransaction(@Body request: CreateTransactionRequest): TransactionResponse

    @GET("transactions/report")
    suspend fun report(): ReportResponse

    @GET("milestones")
    suspend fun milestones(): List<MilestoneResponse>

    @POST("milestones")
    suspend fun createMilestone(@Body request: CreateMilestoneRequest): MilestoneResponse

    @GET("alerts")
    suspend fun alerts(@retrofit2.http.Query("status") status: String? = null): List<AlertResponse>

    @PATCH("alerts/{id}/read")
    suspend fun markAlertRead(@Path("id") id: String): AlertResponse

    @PATCH("alerts/{id}/dismiss")
    suspend fun markAlertDismissed(@Path("id") id: String): AlertResponse
}

data class ReportResponse(
    val totalExpenses: Double = 0.0,
    val weeklyExpenses: Double = 0.0,
    val transactionCount: Int = 0,
    val topCategories: List<CategorySpending> = emptyList(),
)

data class CategorySpending(
    val categoryId: String,
    val categoryName: String,
    val totalAmount: Double,
    val percentage: Int,
)

data class RegisterRequest(

    val email: String,
    val password: String,
    val fullName: String,
)

data class LoginRequest(
    val email: String,
    val password: String,
)

data class RefreshRequest(
    val refreshToken: String,
)

data class AuthResponse(
    val user: AuthUser? = null,
    val accessToken: String,
    val refreshToken: String,
)

data class AuthUser(
    val id: String,
    val email: String,
    val fullName: String,
)

data class CreateAcademicTermRequest(val name: String, val startDate: String, val endDate: String)
data class AcademicTermResponse(val id: String, val name: String, val startDate: String, val endDate: String, val status: String)
data class CreateCategoryRequest(val name: String, val type: String = "expense", val color: String? = null)
data class CategoryResponse(val id: String, val name: String, val type: String, val color: String?)
data class CreateBudgetRequest(
    val amount: Double,
    val periodType: String,
    val startDate: String,
    val endDate: String,
    val currency: String,
    val academicTermId: String,
    val categoryId: String,
)
data class BudgetResponse(val id: String, val amount: String, val periodType: String, val currency: String)
data class UpdateBudgetRequest(val amount: Double? = null, val periodType: String? = null)
data class CreateTransactionRequest(
    val amount: Double,
    val type: String,
    val description: String,
    val occurredAt: String,
    val categoryId: String,
    val academicTermId: String,
    val milestoneId: String? = null,
)
data class TransactionResponse(val id: String, val amount: String, val type: String, val description: String, val occurredAt: String)
data class CreateMilestoneRequest(
    val title: String,
    val description: String? = null,
    val dueDate: String,
    val type: String,
    val priority: Int = 1,
    val academicTermId: String,
)
data class MilestoneResponse(val id: String, val title: String, val dueDate: String, val type: String, val isCompleted: Boolean, val priority: Int)
data class AlertResponse(
    val id: String,
    val type: String,
    val title: String,
    val message: String,
    val severity: String,
    val status: String,
    val triggeredAt: String,
    val budget: BudgetResponse? = null,
    val milestone: MilestoneResponse? = null,
)

object AuthApiFactory {
    fun create(tokenStore: TokenStore): AuthApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = tokenStore.accessToken()
                val request = chain.request().newBuilder().apply {
                    if (!token.isNullOrBlank()) addHeader("Authorization", "Bearer $token")
                }.build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
        .baseUrl(com.sosinhvien.app.BuildConfig.BACKEND_BASE_URL)
            .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
            .create(AuthApi::class.java)
    }
}
