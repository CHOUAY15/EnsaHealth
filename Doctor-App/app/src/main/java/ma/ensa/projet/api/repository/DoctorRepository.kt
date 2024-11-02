package ma.ensa.projet.api.repository

import android.net.Uri
import android.util.Log
import ma.ensa.projet.api.RetrofitClient
import ma.ensa.projet.beans.Doctor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class DoctorRepository {

    fun registerDoctor(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        specialty: String,
        address: String,
        experience: String,
        biography: String,
        imageUri: Uri?,
        imagePath: String?
    ): Call<ResponseBody> {
        val emailBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), email)
        val passwordBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), password)
        val firstNameBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), firstName)
        val lastNameBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), lastName)
        val specialtyBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), specialty)
        val addressBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), address)
        val experienceBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), experience)
        val biographyBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), biography)

        val imagePart: MultipartBody.Part? = if (imageUri != null && imagePath != null) {
            val file = File(imagePath)
            val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
            MultipartBody.Part.createFormData("image", file.name, requestFile)
        } else null

        return RetrofitClient.apiService.registerDoctor(
            emailBody,
            passwordBody,
            firstNameBody,
            lastNameBody,
            specialtyBody,
            addressBody,
            experienceBody,
            biographyBody,
            imagePart
        )
    }

    fun fetchDoctors(onDoctorsFetched: (List<Doctor>) -> Unit, onError: (Throwable) -> Unit) {
        RetrofitClient.apiService.getDoctors().enqueue(object : Callback<List<Doctor>> {
            override fun onResponse(call: Call<List<Doctor>>, response: Response<List<Doctor>>) {
                if (response.isSuccessful) {
                    response.body()?.let { doctors ->
                        onDoctorsFetched(doctors)
                    } ?: Log.e("DoctorRepository", "Response body is null")
                } else {
                    Log.e("DoctorRepository", "Failed to fetch doctors: ${response.message()}")
                    onError(Exception("Failed to fetch doctors: ${response.message()}")) // Call onError on unsuccessful response
                }
            }

            override fun onFailure(call: Call<List<Doctor>>, t: Throwable) {
                Log.e("DoctorRepository", "Error fetching doctors", t)
                onError(t)
            }
        })


    }
}
