package ma.ensa.projet.api

import ma.ensa.projet.api.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    private val publicPaths = listOf(
        "/api/auth/login",
        "/api/auth/doctor/register",
        "/api/auth/patient/register",
        "/api/auth/forgot-password",
        "/api/auth/reset-password"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestUrl = originalRequest.url.encodedPath

        // Check if the path is public (doesn't need authentication)
        if (publicPaths.any { requestUrl.startsWith(it) }) {
            return chain.proceed(originalRequest)
        }

        // For other paths, add token if it exists
        val token = tokenManager.getToken()
        return if (token != null) {
            val authenticatedRequest = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            chain.proceed(authenticatedRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}