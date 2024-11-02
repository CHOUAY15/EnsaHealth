package ma.ensa.projet.api.data

data class AuthResponse(
    val accessToken: String,
    val role: Int,
    val personDto: Person
)

