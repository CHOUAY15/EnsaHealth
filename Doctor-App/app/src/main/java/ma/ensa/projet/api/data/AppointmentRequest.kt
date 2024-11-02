package ma.ensa.projet.api.data

data class AppointmentRequest(
    val date: String,
    val patientId: Int = 1,
    val doctorId: Int = 1,
    val statut: Boolean? = null
)
