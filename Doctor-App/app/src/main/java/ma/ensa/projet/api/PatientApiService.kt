package ma.ensa.projet.api

import ma.ensa.projet.api.data.PatientRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface PatientApiService {
    @POST("/api/auth/patient/register")
    fun registerPatient(@Body patientRegistrationDto: PatientRequest): Call<ResponseBody>

    @PUT("/api/patient/{id}")
    fun updatePatient(
        @Path("id") id: Int,
        @Body patient: PatientRequest
    ): Call<ResponseBody>
}