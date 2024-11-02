package ma.ensa.projet.api

import ma.ensa.projet.api.local.TokenManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:4000"
    private lateinit var tokenManager: TokenManager

    fun initialize(tokenManager: TokenManager) {
        this.tokenManager = tokenManager
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: DoctorApiService by lazy {
        retrofit.create(DoctorApiService::class.java)
    }

    val patientApiService: PatientApiService by lazy {
        retrofit.create(PatientApiService::class.java)
    }

    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val appointmentApiService: AppointmentApiService by lazy {
        retrofit.create(AppointmentApiService::class.java)
    }
}
