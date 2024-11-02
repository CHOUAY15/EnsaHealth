package ma.ensa.projet.ui.doctor

import TimeSlotAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.launch
import ma.ensa.projet.AuthManager
import ma.ensa.projet.R
import ma.ensa.projet.api.RetrofitClient
import ma.ensa.projet.beans.Appointment
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {
    private lateinit var authManager: AuthManager
    private lateinit var calendarView: CalendarView
    private lateinit var timeListView: RecyclerView
    private lateinit var timeAdapter: TimeSlotAdapter
    private lateinit var pieChart: PieChart
    private val rendezvousService = RetrofitClient.appointmentApiService
    private var appointments: List<Appointment> = emptyList()
    private var currentDoctorId: Int = 0

    // Add new properties for toggle functionality
    private var lastSelectedDay: Int = -1
    private var isTimeSlotVisible: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(toolbar)
            supportActionBar?.title = "Calendrier"
        }
        authManager = AuthManager(requireContext())
        currentDoctorId = authManager.getToken()?.let { authManager.getUserId() } ?: 0
        initializeViews(view)
        setupCalendar()
        setupTimeList()
        setupPieChart(view)
        fetchAppointments()
    }

    private fun initializeViews(view: View) {
        calendarView = view.findViewById(R.id.calendarView)
        timeListView = view.findViewById(R.id.timeListView)
        pieChart = view.findViewById(R.id.pieChart)
        timeListView.visibility = View.GONE // Initially hide time slots
    }

    private fun setupCalendar() {
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)

            if (dayOfMonth == lastSelectedDay) {
                // Same day clicked - toggle visibility
                if (isTimeSlotVisible) {
                    hideTimeSlots()
                } else {
                    showTimeSlots(calendar)
                }
            } else {
                // Different day clicked - always show
                showTimeSlots(calendar)
                lastSelectedDay = dayOfMonth
            }
        }
    }

    private fun showTimeSlots(calendar: Calendar) {
        timeListView.visibility = View.VISIBLE
        isTimeSlotVisible = true
        updateTimeSlotsForDate(calendar)
    }

    private fun hideTimeSlots() {
        timeListView.visibility = View.GONE
        isTimeSlotVisible = false
        lastSelectedDay = -1 // Reset last selected day when hiding
    }

    private fun setupTimeList() {
        timeListView.layoutManager = LinearLayoutManager(context)
        timeAdapter = TimeSlotAdapter(generateTimeSlots(),1)
        timeListView.adapter = timeAdapter
    }

    private fun setupPieChart(view: View) {
        pieChart = view.findViewById(R.id.pieChart)
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
        pieChart.dragDecelerationFrictionCoef = 0.95f
        pieChart.isDrawHoleEnabled = false
        pieChart.setTouchEnabled(true)
        pieChart.setDrawEntryLabels(true)
        pieChart.animateY(1400)
    }

    private fun fetchAppointments() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = rendezvousService.getRendezVousByDoctorId(currentDoctorId)
                if (response.isSuccessful) {
                    appointments = response.body() ?: emptyList()
                    updateTimeSlotsForDate(Calendar.getInstance())
                    updatePieChart()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun updatePieChart() {
        val confirmedAppointments = appointments.count { it.statut }
        val pendingAppointments = appointments.count { !it.statut }

        val pieEntries = listOf(
            PieEntry(confirmedAppointments.toFloat(), "Confirmé"),
            PieEntry(pendingAppointments.toFloat(), "Non confirmé")
        )

        val pieDataSet = PieDataSet(pieEntries, "Statut du rendez-vous")
        pieDataSet.colors = listOf(
            resources.getColor(R.color.green, null),
            resources.getColor(R.color.orange, null)
        )

        pieDataSet.valueTextSize = 16f
        pieDataSet.valueTextColor = resources.getColor(R.color.black, null)

        val pieData = PieData(pieDataSet)
        pieData.setValueFormatter { value, _, _, _ ->
            return@setValueFormatter String.format("%.1f%%", value)
        }

        pieChart.data = pieData
        pieChart.invalidate()
    }

    private fun updateTimeSlotsForDate(calendar: Calendar) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDateStr = dateFormat.format(calendar.time)

        val appointmentsForDate = appointments.filter { appointment ->
            appointment.date.startsWith(selectedDateStr)
        }

        timeAdapter.updateAppointments(appointmentsForDate)
    }

    private fun generateTimeSlots(): List<String> {
        val timeSlots = mutableListOf<String>()
        val startHour = 9
        val endHour = 17

        for (hour in startHour until endHour) {
            timeSlots.add(String.format("%02d:00", hour))
        }
        return timeSlots
    }
}