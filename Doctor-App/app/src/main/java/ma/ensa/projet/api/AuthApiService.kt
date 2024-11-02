package ma.ensa.projet.api

import ma.ensa.projet.api.data.AuthResponse
import ma.ensa.projet.api.data.LoginRequest
import ma.ensa.projet.api.data.PasswordResetConfirm
import ma.ensa.projet.api.data.PasswordResetRequest
import okhttp3.ResponseBody

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/password/reset-request")
    suspend fun requestPasswordReset(@Body request: PasswordResetRequest): Response<ResponseBody>

    @POST("/api/password/reset-confirm")
    suspend fun confirmPasswordReset(@Body request: PasswordResetConfirm): Response<ResponseBody>


}