package ma.ensa.projet.api.data

data class PasswordResetRequest(
    val email: String
)

data class PasswordResetConfirm(
    val email: String,
    val token: String,
    val newPassword: String
)