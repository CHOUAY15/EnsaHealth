package ma.ensa.projet.repository

import ma.ensa.projet.api.RetrofitClient
import ma.ensa.projet.api.data.PatientRequest
import okhttp3.ResponseBody
import retrofit2.Call

class PatientRepository {
    fun registerPatient(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNumber: String
    ): Call<ResponseBody> {
        val patientDto = PatientRequest(
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber
        )
        return RetrofitClient.patientApiService.registerPatient(patientDto)
    }
    fun updatePatient(
        id: Int,
        firstName: String,
        lastName: String,
        phoneNumber: String,
    ): Call<ResponseBody> {
        val patientDto = PatientRequest(
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber
        )
        return RetrofitClient.patientApiService.updatePatient(
            id = id,
            patient = patientDto
        )
    }
}
