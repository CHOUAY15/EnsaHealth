package ma.ensa.projet.api

import ma.ensa.projet.api.data.AppointmentRequest
import ma.ensa.projet.beans.Appointment
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AppointmentApiService {
    @POST("/api/rendv")
    suspend fun createRendezVous(@Body request: AppointmentRequest): Response<Unit>
    @GET("/api/rendv/patient/{patientId}")
    suspend fun getRendezVousByPatientId(@Path("patientId") patientId: Int): Response<List<Appointment>>
    @GET("/api/rendv/doctor/{doctorId}")
    suspend fun getRendezVousByDoctorId(@Path("doctorId") doctorId: Int): Response<List<Appointment>>
    @PUT("/api/rendv/{doctorId}/{date}") // Updated to accept doctorId and date
    suspend fun updateRendezVous(
        @Path("doctorId") doctorId: Int,
        @Path("date") date: String, // Use an appropriate format (e.g., "yyyy-MM-dd'T'HH:mm:ss")
        @Body request: AppointmentRequest
    ): Response<Unit>

    @DELETE("/api/rendv/{doctorId}/{date}") // Updated to accept doctorId and date
    suspend fun deleteRendezVous(
        @Path("doctorId") doctorId: Int,
        @Path("date") date: String // Use an appropriate format
    ): Response<Unit>
}