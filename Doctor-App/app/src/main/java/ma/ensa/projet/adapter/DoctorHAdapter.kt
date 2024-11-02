package ma.ensa.projet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ma.ensa.projet.R
import ma.ensa.projet.beans.Doctor

class DoctorHAdapter(
    private val doctorList: List<Doctor>,
    private val showAll: Boolean = false,
    private val onDoctorClick: (Doctor) -> Unit
) : RecyclerView.Adapter<DoctorHAdapter.DoctorViewHolder>() {

    inner class DoctorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val doctorImage: ImageView = view.findViewById(R.id.doctor_image)
        private val doctorName: TextView = view.findViewById(R.id.doctor_name)
        private val doctorSpecialty: TextView = view.findViewById(R.id.doctor_specialty)
        private val doctorExperience: TextView = view.findViewById(R.id.doctor_experience)
        private val experienceIcon: ImageView = view.findViewById(R.id.experience_icon)

        fun bind(doctor: Doctor) {
            Glide.with(itemView.context)
                .load(doctor.getFullImageUrl())
                .into(doctorImage)
            doctorName.text = "Dr.${doctor.fullName}"
            doctorSpecialty.text = doctor.specialty
            doctorExperience.text = "${doctor.yearsExperience} Ans"
            experienceIcon.setImageResource(R.drawable.experience)

            itemView.setOnClickListener { onDoctorClick(doctor) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder =
        DoctorViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_doc, parent, false))

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        holder.bind(doctorList[position])
    }

    override fun getItemCount(): Int = if (showAll) doctorList.size else doctorList.size.coerceAtMost(3)
}
