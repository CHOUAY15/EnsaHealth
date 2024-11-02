package ma.ensa.projet

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ma.ensa.projet.api.AuthApiService
import ma.ensa.projet.api.RetrofitClient
import ma.ensa.projet.api.data.AuthResponse
import ma.ensa.projet.api.data.LoginRequest
import ma.ensa.projet.api.data.PasswordResetConfirm
import ma.ensa.projet.api.data.PasswordResetRequest
import ma.ensa.projet.api.data.Person

class AuthManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val api: AuthApiService = RetrofitClient.authApiService

    val isLoggedIn: Boolean
        get() = getToken() != null

    fun getToken(): String? = prefs.getString("token", null)

    fun getUserName(): String = "${prefs.getString("first_name", "")} ${prefs.getString("last_name", "")}"

    fun getUserRole(): Int = prefs.getInt("user_role", -1)
    fun getUserId(): Int = prefs.getInt("person_id", -1)
    fun getDocSpec(): String? = prefs.getString("specialty", null)

    fun getUser(): Person? = if (isLoggedIn) {
        Person(
            id = getUserId(),
            firstName = prefs.getString("first_name", "") ?: "",
            lastName = prefs.getString("last_name", "") ?: "",
            phoneNumber = prefs.getString("phone_number", null),
            specialty = prefs.getString("specialty", null),
            address = prefs.getString("address", null),
            biography = prefs.getString("biography", null),
            yearsExperience = prefs.getInt("years_experience", -1).takeIf { it != -1 },
            image = prefs.getString("image", null)
        )
    } else null

    fun updateUserData(person: Person) {
        prefs.edit().apply {
            putString("first_name", person.firstName)
            putString("last_name", person.lastName)
            putString("phone_number", person.phoneNumber)
            putString("specialty", person.specialty)
            putString("address", person.address)
            putString("biography", person.biography)
            person.yearsExperience?.let { putInt("years_experience", it) }
            putString("image", person.image)
            apply()
        }
    }

    suspend fun login(email: String, password: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = api.login(LoginRequest(email, password))
                if (response.isSuccessful) {

                    response.body()?.let { saveUserData(it) } ?: throw Exception("Login failed")

                } else {
                    throw Exception(response.errorBody()?.string() ?: "Unknown error")
                }
            } catch (e: Exception) {
                throw Exception("Login failed: ${e.message}")
            }
        }
    }
    suspend fun requestPasswordReset(email: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = api.requestPasswordReset(PasswordResetRequest(email))
                if (!response.isSuccessful) {
                    throw Exception(response.errorBody()?.string() ?: "Password reset request failed")
                }

            } catch (e: Exception) {
                throw Exception("Password reset request failed: ${e.message}")
            }
        }
    }

    suspend fun confirmPasswordReset(email: String, token: String, newPassword: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = api.confirmPasswordReset(
                    PasswordResetConfirm(
                        email = email,
                        token = token,
                        newPassword = newPassword
                    )
                )
                if (!response.isSuccessful) {
                    throw Exception(response.errorBody()?.string() ?: "Password reset confirmation failed")
                }
                // La réponse est réussie, mais nous n'avons pas besoin de parser le corps
                // car nous savons que c'est juste un message texte de succès
            } catch (e: Exception) {
                throw Exception("Password reset confirmation failed: ${e.message}")
            }
        }
    }


    fun logout() {
        prefs.edit().clear().apply()
    }

    private fun saveUserData(authResponse: AuthResponse) {
        Log.d("Token","${authResponse.accessToken}")
        prefs.edit().apply {
            putString("token", authResponse.accessToken)
            putInt("user_role", authResponse.role)
            putInt("person_id", authResponse.personDto.id)
            putString("first_name", authResponse.personDto.firstName)
            putString("last_name", authResponse.personDto.lastName)
            authResponse.personDto.phoneNumber?.let { putString("phone_number", it) }
            authResponse.personDto.specialty?.let { putString("specialty", it) }
            authResponse.personDto.address?.let { putString("address", it) }
            authResponse.personDto.yearsExperience?.let { putInt("years_experience", it) }
            authResponse.personDto.biography?.let { putString("biography", it) }
            authResponse.personDto.image?.let { putString("image", it) }
            apply()
        }
    }
}
