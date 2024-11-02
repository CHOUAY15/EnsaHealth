package ma.ensa.projet.beans

import java.io.Serializable

data class Doctor(
    val id:Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phone: String,
    val specialty: String,
    val address: String,
    val biography:String,
    val yearsExperience: Int,
    val image: String
) : Serializable {
    val fullName: String
        get() = "$firstName $lastName"




    fun getFullImageUrl(): String {
        return "http://10.0.2.2:4000/api/images/$image"
    }
}
