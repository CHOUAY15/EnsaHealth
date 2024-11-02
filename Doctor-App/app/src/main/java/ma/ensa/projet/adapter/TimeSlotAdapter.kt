import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ma.ensa.projet.R
import ma.ensa.projet.beans.Appointment
import java.text.SimpleDateFormat
import java.util.Locale

class TimeSlotAdapter(
    private val timeSlots: List<String>, private val nm: Int
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    private var onTimeSlotClickListener: ((String) -> Unit)? = null
    private var appointments: List<Appointment> = emptyList()
    // Add selected time slot tracking
    private var selectedTimeSlot: String? = null

    fun setOnTimeSlotClickListener(listener: (String) -> Unit) {
        onTimeSlotClickListener = listener
    }

    fun updateAppointments(newAppointments: List<Appointment>) {
        appointments = newAppointments
        selectedTimeSlot = null  // Reset selection when appointments update
        notifyDataSetChanged()
    }

    private fun isTimeSlotBooked(timeSlot: String): Boolean {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        return appointments.any { appointment ->
            val appointmentTime = timeFormat.format(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    .parse(appointment.date)
            )
            if (nm == 0) {
                appointmentTime == timeSlot
            } else {
                appointmentTime == timeSlot && appointment.statut
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val timeSlot = timeSlots[position]
        holder.bind(timeSlot, isTimeSlotBooked(timeSlot), timeSlot == selectedTimeSlot)
    }

    override fun getItemCount() = timeSlots.size

    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)

        fun bind(time: String, isBooked: Boolean, isSelected: Boolean) {
            timeTextView.text = time

            when {
                isBooked -> {
                    timeTextView.text = "$time réservé"
                    timeTextView.setTextColor(Color.RED)
                    itemView.isClickable = false
                    itemView.background = ColorDrawable(Color.LTGRAY)
                }
                isSelected -> {
                    // Add selected state styling
                    timeTextView.setTextColor(Color.WHITE)
                    itemView.isClickable = true
                    itemView.background = ColorDrawable(Color.parseColor("#9868d3"))
                }
                else -> {
                    timeTextView.setTextColor(Color.BLACK)
                    itemView.isClickable = true
                    val typedValue = TypedValue()
                    itemView.context.theme.resolveAttribute(
                        android.R.attr.selectableItemBackground,
                        typedValue,
                        true
                    )
                    itemView.setBackgroundResource(typedValue.resourceId)
                }
            }

            itemView.setOnClickListener {
                if (!isBooked) {
                    selectedTimeSlot = time
                    onTimeSlotClickListener?.invoke(time)
                    notifyDataSetChanged() // Refresh all items to update selection
                }
            }
        }
    }
}