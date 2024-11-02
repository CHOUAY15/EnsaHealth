package ma.ensa.projet.api.data

data class PatientRequest(
    val email: String? = null,
    val password: String? = null,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String
)
