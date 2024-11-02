package ma.ensa.projet.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ma.ensa.projet.R
import ma.ensa.projet.beans.Doctor

class DoctorVAdapter(
    private val doctorList: List<Doctor>,
    private val context: Context,
    private val onAppointmentClick: (Doctor) -> Unit,
    private val onDoctorClick: (Doctor) -> Unit
) : RecyclerView.Adapter<DoctorVAdapter.DoctorViewHolder>() {

    inner class DoctorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val doctorImage: ImageView = view.findViewById(R.id.doctor_image)
        private val doctorName: TextView = view.findViewById(R.id.doctor_name)
        private val doctorSpecialty: TextView = view.findViewById(R.id.doctor_specialty)
        private val appointmentButton: Button = view.findViewById(R.id.btn_appointment)

        fun bind(doctor: Doctor) {
            Glide.with(itemView.context)
                .load(doctor.getFullImageUrl())
                .into(doctorImage)
            doctorName.text = "Dr.${doctor.fullName}"
            doctorSpecialty.text = doctor.specialty

            appointmentButton.setOnClickListener { onAppointmentClick(doctor) }
            itemView.setOnClickListener { onDoctorClick(doctor) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder =
        DoctorViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_doc2, parent, false))

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        holder.bind(doctorList[position])
    }

    override fun getItemCount(): Int = doctorList.size
}
