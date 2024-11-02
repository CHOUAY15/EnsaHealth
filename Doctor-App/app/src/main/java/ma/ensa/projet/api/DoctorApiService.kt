package ma.ensa.projet.api

import ma.ensa.projet.beans.Doctor
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface DoctorApiService {

    @Multipart
    @POST("/api/auth/doctor/register")
    fun registerDoctor(
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part("firstName") firstName: RequestBody,
        @Part("lastName") lastName: RequestBody,
        @Part("specialty") specialty: RequestBody,
        @Part("address") address: RequestBody,
        @Part("yearsExperience") yearsExperience: RequestBody,
        @Part("biography") biography: RequestBody,
        @Part image: MultipartBody.Part?
    ): Call<ResponseBody>

    @GET("/api/doctors")
    fun getDoctors(): Call<List<Doctor>>

    @GET("/api/doctors/specialty/{specialty}")
    fun getDoctorsBySpecialty(@Path("specialty") specialty: String): Call<List<Doctor>>

    @Multipart
    @PUT("/api/doctors/{id}")
    fun updateDoctor(
        @Path("id") id: Int,
        @Part("firstName") firstName: RequestBody,
        @Part("lastName") lastName: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("specialty") specialty: RequestBody,
        @Part("address") address: RequestBody,
        @Part("biography") biography: RequestBody,
        @Part("yearsExperience") yearsExperience: RequestBody,
        @Part image: MultipartBody.Part?
    ): Call<ResponseBody>
}
