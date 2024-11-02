package ma.ensa.projet.api.repository
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ma.ensa.projet.api.RetrofitClient
import ma.ensa.projet.api.data.AppointmentRequest
import ma.ensa.projet.beans.Appointment
import org.json.JSONObject
import retrofit2.Response

class AppointmentRepository(private val context: Context) {

    private val rendezvousService = RetrofitClient.appointmentApiService

    suspend fun fetchAppointmentsByPatientId(patientId: Int): Result<List<Appointment>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = rendezvousService.getRendezVousByPatientId(patientId)
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(Exception("Failed to fetch appointments: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun fetchAppointmentsByDoctorId(doctorId: Int): Result<List<Appointment>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = rendezvousService.getRendezVousByDoctorId(doctorId)
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(Exception("Failed to fetch appointments: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteAppointment(doctorId: Int, date: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = rendezvousService.deleteRendezVous(doctorId, date)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    val errorMessage = getErrorMessage(response)
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateAppointment(
        doctorId: Int, originalDate: String, request: AppointmentRequest
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = rendezvousService.updateRendezVous(doctorId, originalDate, request)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    val errorMessage = getErrorMessage(response)
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun getErrorMessage(response: Response<Unit>): String {
        return try {
            JSONObject(response.errorBody()?.string()).getString("message")
        } catch (e: Exception) {
            "An error occurred during the request"
        }
    }
}
