package ma.ensa.projet

import TimeSlotAdapter
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import ma.ensa.projet.api.RetrofitClient
import ma.ensa.projet.api.data.AppointmentRequest
import ma.ensa.projet.api.repository.AppointmentRepository
import ma.ensa.projet.beans.Appointment
import ma.ensa.projet.beans.Doctor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DoctorDetaillActivity : AppCompatActivity() {
    private lateinit var authManager: AuthManager
    private lateinit var appointmentRepository: AppointmentRepository
    private val rendezvousService = RetrofitClient.appointmentApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_doctor_detaill)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeComponents()
        setupDoctorDetails()
        setupButtons()
    }

    private fun initializeComponents() {
        authManager = AuthManager(this)
        appointmentRepository = AppointmentRepository(this)
    }

    private fun setupDoctorDetails() {
        val doctor = intent.getSerializableExtra("selected_doctor") as? Doctor

        doctor?.let {
            findViewById<ImageView>(R.id.doctorImage).apply {
                Glide.with(this@DoctorDetaillActivity).load(it.getFullImageUrl()).into(this)
            }
            findViewById<TextView>(R.id.doctorName).text = "Dr.${it.fullName}"
            findViewById<TextView>(R.id.doctorSpecialty).text = it.specialty
            findViewById<TextView>(R.id.doctorAddress).text = it.address
            findViewById<TextView>(R.id.biographyContent).text = it.biography
            findViewById<TextView>(R.id.expt).text = "Expérience\n${it.yearsExperience} Ans +"
        }
    }

    private fun setupButtons() {
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.makeAppointmentButton).setOnClickListener {
            val doctor = intent.getSerializableExtra("selected_doctor") as? Doctor
            doctor?.let { showAppointmentDialog(it) }
        }
    }

    private data class DialogComponents(
        val selectDateButton: MaterialButton,
        val confirmButton: MaterialButton,
        val selectedDateText: TextView,
        val timeSlotRecyclerView: RecyclerView,
        var selectedTime: String? = null
    )

    private fun showAppointmentDialog(doctor: Doctor) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.appointment_dialog)

        val dialogComponents = initializeDialogComponents(dialog)
        setupTimeSlotRecyclerView(dialogComponents.timeSlotRecyclerView, doctor, dialogComponents)
        setupDateSelection(dialogComponents, doctor)
        setupConfirmButton(dialogComponents, doctor)

        dialog.show()
    }

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
                val response = rendezvousService.createRendezVous(request)
                if (response.isSuccessful) {
                    showSuccessDialog(date, hour)
                } else {
                    showErrorDialog("Cette date de rendez-vous est déjà réservée.")
                }
            } catch (e: Exception) {
                Log.e("RendezVous", "Erreur: ${e.message}")
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