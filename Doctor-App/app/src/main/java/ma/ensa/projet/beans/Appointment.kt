package ma.ensa.projet.beans

data class Appointment(
    val id:Int,
    val patientId: Int,
    val doctorId: Int,
    val date: String,
    val ptienName: String,
    val doctorName: String,
    val statut: Boolean
)
