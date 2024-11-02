package ma.ensa.projet.api.data

data class Person(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String? = null,
    val specialty: String? = null,
    val address: String? = null,
    val yearsExperience: Int? = null,
    val biography: String? = null,
    val image: String? = null
) {
    fun getFullImageUrl(): String {
        return "http://10.0.2.2:4000/api/images/$image"
    }
}
