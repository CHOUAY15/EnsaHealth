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

class AppointmentAdapter(
    private var appointments: MutableList<Appointment>,
    private val onItemClick: (Appointment) -> Unit,
    private val onDownloadClick: (Appointment) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val doctorName: TextView = itemView.findViewById(R.id.tvDoctorName)
        val appointmentDate: TextView = itemView.findViewById(R.id.tvAppointmentDate)
        val appointmentStatus: TextView = itemView.findViewById(R.id.tvAppointmentStatus)
        val downloadButton: ImageView = itemView.findViewById(R.id.download)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder =
        AppointmentViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_appoi, parent, false)
        )

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        appointments[position].let { appointment ->
            holder.apply {
                doctorName.text = "Dr.${appointment.doctorName}"
                appointmentDate.text = formatDate(appointment.date)
                appointmentStatus.apply {
                    text = if (appointment.statut) "Approuvé" else "En attente"
                    setTextColor(if (appointment.statut) Color.GREEN else Color.parseColor("#FFA500"))
                }
                downloadButton.apply {
                    visibility = if (appointment.statut) View.VISIBLE else View.GONE
                    setOnClickListener {
                        // Only trigger download for approved appointments
                        if (appointment.statut) {
                            onDownloadClick(appointment)
                        }
                    }
                }
                itemView.setOnClickListener { onItemClick(appointment) }
            }
        }
    }

    override fun getItemCount() = appointments.size

    fun removeItem(position: Int): Appointment =
        appointments.removeAt(position).also { notifyItemRemoved(position) }

    fun restoreItem(appointment: Appointment, position: Int) {
        appointments.add(position, appointment)
        notifyItemInserted(position)
    }

    fun updateAppointments(newAppointments: List<Appointment>) {
        appointments.clear()
        appointments.addAll(newAppointments)
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