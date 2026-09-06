package com.sosinhvien.app.data.network.compose

import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthApiTest {
    @Test
    fun authRequestsSendJsonBodies() = runBlocking {
        val requests = mutableListOf<Pair<String, String>>()
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request()
            val buffer = Buffer()
            request.body!!.writeTo(buffer)
            assertEquals("POST", request.method)
            assertEquals("application/json; charset=UTF-8", request.body!!.contentType().toString())
            requests.add(request.url.encodedPath to buffer.readUtf8())
            Response.Builder().request(request).protocol(Protocol.HTTP_1_1)
                .code(200).message("OK")
                .body("""{"accessToken":"access","refreshToken":"refresh"}""".toResponseBody())
                .build()
        }.build()
        // Parse every Retrofit declaration, so missing annotations fail this test.
        val api = Retrofit.Builder().baseUrl("http://localhost/api/v1/")
            .client(client).addConverterFactory(GsonConverterFactory.create())
            .validateEagerly(true).build().create(AuthApi::class.java)
        assertEquals("access", api.register(RegisterRequest("test@example.com", "password", "Student")).accessToken)
        api.login(LoginRequest("test@example.com", "password"))
        api.refresh(RefreshRequest("refresh-token"))
        assertEquals(listOf("/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/refresh"), requests.map { it.first })
        val bodies = requests.map { JsonParser.parseString(it.second).asJsonObject }
        assertEquals("Student", bodies[0].get("fullName").asString)
        assertEquals("test@example.com", bodies[0].get("email").asString)
        assertEquals("password", bodies[0].get("password").asString)
        assertEquals("test@example.com", bodies[1].get("email").asString)
        assertEquals("password", bodies[1].get("password").asString)
        assertEquals("refresh-token", bodies[2].get("refreshToken").asString)
    }
}
