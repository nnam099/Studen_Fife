package com.sosinhvien.app.data.auth

import com.sosinhvien.app.data.network.compose.AuthApi
import com.sosinhvien.app.data.network.compose.AuthApiFactory
import com.sosinhvien.app.data.network.compose.AuthResponse
import com.sosinhvien.app.data.network.compose.LoginRequest
import com.sosinhvien.app.data.network.compose.RefreshRequest
import com.sosinhvien.app.data.network.compose.RegisterRequest
import com.sosinhvien.app.data.network.compose.AcademicTermResponse
import com.sosinhvien.app.data.network.compose.BudgetResponse
import com.sosinhvien.app.data.network.compose.CategoryResponse
import com.sosinhvien.app.data.network.compose.CreateAcademicTermRequest
import com.sosinhvien.app.data.network.compose.CreateBudgetRequest
import com.sosinhvien.app.data.network.compose.CreateCategoryRequest
import com.sosinhvien.app.data.network.compose.CreateMilestoneRequest
import com.sosinhvien.app.data.network.compose.CreateTransactionRequest
import com.sosinhvien.app.data.network.compose.MilestoneResponse
import com.sosinhvien.app.data.network.compose.TransactionResponse

class AuthRepository(
    private val tokenStore: TokenStore,
    private val api: AuthApi = AuthApiFactory.create(tokenStore),
) {
    suspend fun register(email: String, password: String, fullName: String): AuthResponse {
        return api.register(RegisterRequest(email, password, fullName)).also(::save)
    }

    suspend fun login(email: String, password: String): AuthResponse {
        return api.login(LoginRequest(email, password)).also(::save)
    }

    suspend fun refresh(): AuthResponse {
        val refreshToken = tokenStore.refreshToken() ?: error("No refresh token saved")
        return api.refresh(RefreshRequest(refreshToken)).also(::save)
    }

    suspend fun academicTerms(): List<AcademicTermResponse> = api.academicTerms()
    suspend fun createAcademicTerm(name: String, startDate: String, endDate: String) =
        api.createAcademicTerm(CreateAcademicTermRequest(name, startDate, endDate))
    suspend fun categories(): List<CategoryResponse> = api.categories()
    suspend fun createCategory(name: String) = api.createCategory(CreateCategoryRequest(name))
    suspend fun budgets(): List<BudgetResponse> = api.budgets()
    suspend fun createBudget(request: CreateBudgetRequest) = api.createBudget(request)
    suspend fun transactions(): List<TransactionResponse> = api.transactions()
    suspend fun createTransaction(request: CreateTransactionRequest) = api.createTransaction(request)
    suspend fun milestones(): List<MilestoneResponse> = api.milestones()
    suspend fun createMilestone(request: CreateMilestoneRequest) = api.createMilestone(request)

    private fun save(response: AuthResponse) {
        tokenStore.save(response.accessToken, response.refreshToken)
    }
}
