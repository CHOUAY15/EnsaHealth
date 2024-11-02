package ma.ensa.projet

import TimeSlotAdapter
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import ma.ensa.projet.adapter.DoctorVAdapter
import ma.ensa.projet.api.RetrofitClient
import ma.ensa.projet.api.data.AppointmentRequest
import ma.ensa.projet.api.repository.AppointmentRepository
import ma.ensa.projet.beans.Appointment
import ma.ensa.projet.beans.Doctor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ListDoctorActivity : AppCompatActivity() {
    private lateinit var authManager: AuthManager
    private lateinit var appointmentRepository: AppointmentRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_doctor)

        initializeComponents()
        setupRecyclerView()
        setupBackButton()
    }

    private fun initializeComponents() {
        authManager = AuthManager(this)
        appointmentRepository = AppointmentRepository(this)
    }

    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.rvDoctorsFullList)
        val doctors: ArrayList<Doctor>? = intent.getSerializableExtra("DOCTORS_LIST") as? ArrayList<Doctor>

        val adapter = DoctorVAdapter(
            doctors ?: emptyList(),
            this,
            { doctor -> showAppointmentDialog(doctor) },
            { doctor -> navigateToDoctorDetail(doctor) }
        )

        recyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(this@ListDoctorActivity)
        }
    }

    private fun setupBackButton() {
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun navigateToDoctorDetail(doctor: Doctor) {
        val intent = Intent(this, DoctorDetaillActivity::class.java).apply {
            putExtra("selected_doctor", doctor)
        }
        startActivity(intent)
    }

    private fun showAppointmentDialog(doctor: Doctor) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.appointment_dialog)

        val dialogComponents = initializeDialogComponents(dialog)
        setupTimeSlotRecyclerView(dialogComponents.timeSlotRecyclerView, doctor, dialogComponents)
        setupDateSelection(dialogComponents, doctor)
        setupConfirmButton(dialogComponents, doctor)

        dialog.show()
    }

    private data class DialogComponents(
        val selectDateButton: MaterialButton,
        val confirmButton: MaterialButton,
        val selectedDateText: TextView,
        val timeSlotRecyclerView: RecyclerView,
        var selectedTime: String? = null
    )

    private fun initializeDialogComponents(dialog: Dialog): DialogComponents {
        return DialogComponents(
            selectDateButton = dialog.findViewById(R.id.selectDateButton),
            confirmButton = dialog.findViewById(R.id.confirmButton),
            selectedDateText = dialog.findViewById(R.id.selectedDateText),
            timeSlotRecyclerView = dialog.findViewById(R.id.timeSlotRecyclerView)
        )
    }

    private fun setupTimeSlotRecyclerView(
        recyclerView: RecyclerView,
        doctor: Doctor,
        dialogComponents: DialogComponents
    ) {
        recyclerView.layoutManager = LinearLayoutManager(this)
        val timeSlots = listOf("09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00")
        val adapter = TimeSlotAdapter(timeSlots,0)
        recyclerView.adapter = adapter

        // Initially hide the RecyclerView
        recyclerView.visibility = View.GONE

        adapter.setOnTimeSlotClickListener { time ->
            dialogComponents.selectedTime = time
            dialogComponents.confirmButton.isEnabled = true
        }
    }

    private fun setupDateSelection(dialogComponents: DialogComponents, doctor: Doctor) {
        dialogComponents.selectDateButton.setOnClickListener {
            showDatePicker(dialogComponents, doctor)
        }
    }

    private fun showDatePicker(dialogComponents: DialogComponents, doctor: Doctor) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = "$dayOfMonth/${month + 1}/$year"
                dialogComponents.selectedDateText.text = selectedDate
                // Show RecyclerView when date is selected
                dialogComponents.timeSlotRecyclerView.visibility = View.VISIBLE
                fetchAppointmentsForDate(doctor, selectedDate, dialogComponents.timeSlotRecyclerView)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun fetchAppointmentsForDate(doctor: Doctor, selectedDate: String, recyclerView: RecyclerView) {
        lifecycleScope.launch {
            try {
                val result = appointmentRepository.fetchAppointmentsByDoctorId(doctor.id)
                result.onSuccess { appointments ->
                    val filteredAppointments = filterAppointmentsByDate(appointments, selectedDate)
                    (recyclerView.adapter as? TimeSlotAdapter)?.updateAppointments(filteredAppointments)
                }.onFailure {
                    showErrorDialog("Erreur de connexion")
                }
            } catch (e: Exception) {
                showErrorDialog("Erreur de connexion")
            }
        }
    }

    private fun filterAppointmentsByDate(appointments: List<Appointment>, selectedDate: String): List<Appointment> {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val selectedDateTime = dateFormat.parse(selectedDate)
        return appointments.filter {
            val appointmentDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .parse(it.date)
            dateFormat.format(appointmentDate) == dateFormat.format(selectedDateTime)
        }
    }

    private fun setupConfirmButton(dialogComponents: DialogComponents, doctor: Doctor) {
        dialogComponents.confirmButton.setOnClickListener {
            dialogComponents.selectedTime?.let { time ->
                createAppointment(
                    doctor,
                    dialogComponents.selectedDateText.text.toString(),
                    time
                )
                (dialogComponents.confirmButton.parent as? Dialog)?.dismiss()
            }
        }
    }

    private fun createAppointment(doctor: Doctor, date: String, hour: String) {
        val dateTimeStr = formatDateTime(date, hour)
        val patientId = authManager.getUserId() ?: 0

        val request = AppointmentRequest(
            date = dateTimeStr,
            patientId = patientId,
            doctorId = doctor.id
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.appointmentApiService.createRendezVous(request)
                if (response.isSuccessful) {
                    showSuccessDialog(date, hour)
                } else {
                    showErrorDialog("Cette date de rendez-vous est déjà réservée.")
                }
            } catch (e: Exception) {
                showErrorDialog("Erreur de connexion")
            }
        }
    }

    private fun formatDateTime(date: String, hour: String): String {
        val parts = date.split("/")
        return "${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}T$hour:00"
    }

    private fun showSuccessDialog(date: String, hour: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.confirmation_dialog)

        val appointmentDetails = dialog.findViewById<TextView>(R.id.appointmentDetails)
        val okButton = dialog.findViewById<MaterialButton>(R.id.okButton)

        appointmentDetails.text = "Rendez-vous confirmé pour:\nDate: $date\nHeure: $hour"
        okButton.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showErrorDialog(message: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.error_dialog)

        val errorMessage = dialog.findViewById<TextView>(R.id.errorMessage)
        val okButton = dialog.findViewById<MaterialButton>(R.id.okButton)

        errorMessage.text = message
        okButton.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}