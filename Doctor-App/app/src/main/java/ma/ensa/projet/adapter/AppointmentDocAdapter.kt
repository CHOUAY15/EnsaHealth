package ma.ensa.projet.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ma.ensa.projet.R
import ma.ensa.projet.beans.Appointment
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class AppointmentDocAdapter(
    private var appointments: MutableList<Appointment>,
    private val onItemClick: (Appointment) -> Unit
) : RecyclerView.Adapter<AppointmentDocAdapter.AppointmentViewHolder>() {

    fun removeItem(position: Int): Appointment = appointments.removeAt(position).also {
        notifyItemRemoved(position)
    }

    fun restoreItem(appointment: Appointment, position: Int) {
        appointments.add(position, appointment)
        notifyItemInserted(position)
    }

    inner class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val patientName: TextView = itemView.findViewById(R.id.tvPatientName)
        val tvReason: TextView = itemView.findViewById(R.id.tvReason)
        val scheduleIcon: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(appointment: Appointment) {
            patientName.text = appointment.ptienName ?: "Unknown Patient"
            tvReason.text = formatDate(appointment.date)
            scheduleIcon.setColorFilter(if (appointment.statut) Color.GREEN else Color.parseColor("#FFA500"))
            itemView.setOnClickListener { onItemClick(appointment) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder =
        AppointmentViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_appoi_doc, parent, false)
        )

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        holder.bind(appointments[position])
    }

    override fun getItemCount() = appointments.size

    fun updateAppointments(newAppointments: List<Appointment>) {
        appointments.apply {
            clear()
            addAll(newAppointments)
        }
        notifyDataSetChanged()
    }

    private fun formatDate(dateString: String): String {
        // Define the input format
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        // Parse the date string to a LocalDateTime object
        val dateTime = LocalDateTime.parse(dateString, inputFormatter)

        // Define the output format - Fixed by removing invalid 'r' pattern and properly escaping "heure"
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd, 'heure:' HH:mm:ss")
        // Format the LocalDateTime object to the desired format
        return dateTime.format(outputFormatter)
    }
}
