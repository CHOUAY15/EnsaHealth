package ma.ensa.projet.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import ma.ensa.projet.ListDoctorActivity
import ma.ensa.projet.R
import ma.ensa.projet.api.RetrofitClient
import ma.ensa.projet.beans.Doctor
import ma.ensa.projet.beans.Speciality
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SpecialityAdapter(
    private val specialityList: List<Speciality>,
    private val context: Context
) : RecyclerView.Adapter<SpecialityAdapter.SpecialityViewHolder>() {

    private val apiService = RetrofitClient.apiService

    class SpecialityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val specialityIcon: ImageView = itemView.findViewById(R.id.speciality_icon)
        val specialityName: TextView = itemView.findViewById(R.id.speciality_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecialityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_spe, parent, false)
        return SpecialityViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpecialityViewHolder, position: Int) {
        val speciality = specialityList[position]

        holder.specialityIcon.setImageResource(speciality.icon)
        holder.specialityName.text = speciality.name

        // Set click listener for the item
        holder.itemView.setOnClickListener {
            fetchDoctorsBySpecialty(speciality.name)
        }
    }

    override fun getItemCount(): Int = specialityList.size

    private fun fetchDoctorsBySpecialty(specialty: String) {
        apiService.getDoctorsBySpecialty(specialty).enqueue(object : Callback<List<Doctor>> {
            override fun onResponse(call: Call<List<Doctor>>, response: Response<List<Doctor>>) {
                if (response.isSuccessful) {
                    response.body()?.let { doctors ->
                        // Start ListDoctorActivity with the filtered doctors
                        val intent = Intent(context, ListDoctorActivity::class.java).apply {
                            putExtra("DOCTORS_LIST", ArrayList(doctors))
                            putExtra("SPECIALTY", specialty) // Optional: to show the specialty in the list
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK // Required when starting activity from adapter
                        }
                        context.startActivity(intent)
                    }
                } else {
                    // Show error message
                    Toast.makeText(
                        context,
                        "Failed to fetch doctors: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Doctor>>, t: Throwable) {
                Toast.makeText(
                    context,
                    "Error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}