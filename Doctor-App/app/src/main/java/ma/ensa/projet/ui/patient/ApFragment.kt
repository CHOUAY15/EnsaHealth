package ma.ensa.projet.ui.patient

import TimeSlotAdapter
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.widget.SearchView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ma.ensa.projet.AuthManager
import ma.ensa.projet.R
import ma.ensa.projet.adapter.AppointmentAdapter
import ma.ensa.projet.api.RetrofitClient
import ma.ensa.projet.api.data.AppointmentRequest
import ma.ensa.projet.api.repository.AppointmentRepository
import ma.ensa.projet.beans.Appointment
import ma.ensa.projet.databinding.FragmentAppoinBinding
import ma.ensa.projet.util.AppointmentPdfService
import ma.ensa.projet.util.SwipeGestureCallback
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ApFragment : Fragment() {
    private var _binding: FragmentAppoinBinding? = null
    private val binding get() = _binding!!

    private lateinit var authManager: AuthManager
    private val rendezvousService = RetrofitClient.appointmentApiService
    private lateinit var pdfService: AppointmentPdfService
    private var currentPatientId: Int = 0

    private var isAscendingOrder = true
    private var currentFilter = "ALL"
    private var currentSearchQuery = ""
    private var isRefreshing = false

    private lateinit var appointmentAdapter: AppointmentAdapter
    private var appointments = mutableListOf<Appointment>()
    private lateinit var appointmentRepository: AppointmentRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appointmentRepository = AppointmentRepository(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAppoinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        setupInitialState()
        setupSwipeToDelete()
        setupFilterChips()
        fetchAppointments()
    }

    private fun setupInitialState() {
        pdfService = AppointmentPdfService(requireContext())
        authManager = AuthManager(requireContext())
        currentPatientId = authManager.getToken()?.let { authManager.getUserId() } ?: 0
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        setupRecyclerView()
    }

    // In your ApFragment.kt
    private fun handlePdfDownload(appointment: Appointment) {
        lifecycleScope.launch {
            try {
                pdfService.generatePdfFromHtml(appointment)
                    .onSuccess { file ->
                        val uri = FileProvider.getUriForFile(
                            requireContext(),
                            "${requireContext().packageName}.provider",
                            file
                        )

                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }

                        startActivity(Intent.createChooser(intent, "Ouvrir avec..."))
                    }
                    .onFailure { error ->
                        showErrorDialog("Erreur lors de la génération du PDF: ${error.message}")
                    }
            } catch (e: Exception) {
                showSuccessDialog("Votre invitation est prête")
            }
        }
    }

    private fun setupRecyclerView() {
        appointmentAdapter = AppointmentAdapter(
            appointments = appointments,
            onItemClick = { appointment ->
                showUpdateAppointmentDialog(appointment)
            },
            onDownloadClick = { appointment ->
                handlePdfDownload(appointment)
            }
        )
        binding.recyclerViewAppointments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = appointmentAdapter
        }
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.appointment_menu, menu)
                setupSearchView(menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return handleMenuItemSelection(menuItem)
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupSearchView(menu: Menu) {
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentSearchQuery = query ?: ""
                filterAndSortAppointments()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText ?: ""
                filterAndSortAppointments()
                return true
            }
        })
    }

    private fun handleMenuItemSelection(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_filter -> {
                toggleFilterChips()
                true
            }
            R.id.action_refresh -> {
                if (!isRefreshing) refreshAppointments()
                true
            }
            R.id.action_sort -> {
                showSortDialog()
                true
            }
            else -> false
        }
    }

    private fun setupFilterChips() {
        binding.apply {
            chipAll.text = "TOUT"
            chipConfirmed.text = "CONFIRMÉ"
            chipPending.text = "EN ATTENTE"

            chipAll.setOnClickListener {
                currentFilter = "ALL"
                filterAndSortAppointments()
            }
            chipConfirmed.setOnClickListener {
                currentFilter = "CONFIRMED"
                filterAndSortAppointments()
            }
            chipPending.setOnClickListener {
                currentFilter = "PENDING"
                filterAndSortAppointments()
            }
        }
    }

    private fun toggleFilterChips() {
        binding.filterChipGroup.visibility = if (binding.filterChipGroup.visibility == View.VISIBLE) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun showSortDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_sort)

        val radioGroup = dialog.findViewById<RadioGroup>(R.id.sortRadioGroup)
        radioGroup.check(if (isAscendingOrder) R.id.sortDateAsc else R.id.sortDateDesc)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            isAscendingOrder = checkedId == R.id.sortDateAsc
            filterAndSortAppointments()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun filterAndSortAppointments() {
        val filteredList = appointments.filter { appointment ->
            val matchesSearch = if (currentSearchQuery.isEmpty()) {
                true
            } else {
                appointment.doctorName.contains(currentSearchQuery, ignoreCase = true)
            }

            val matchesStatus = when (currentFilter) {
                "CONFIRMED" -> appointment.statut
                "PENDING" -> !appointment.statut
                else -> true
            }

            matchesSearch && matchesStatus
        }

        val sortedList = if (isAscendingOrder) {
            filteredList.sortedBy { it.date }
        } else {
            filteredList.sortedByDescending { it.date }
        }

        appointmentAdapter.updateAppointments(sortedList)
    }

    private fun refreshAppointments() {
        isRefreshing = true
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerViewAppointments.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = rendezvousService.getRendezVousByPatientId(currentPatientId)
                delay(2000)

                if (response.isSuccessful) {
                    val allAppointments = response.body() ?: emptyList()
                    handleSuccessfulFetch(allAppointments)
                } else {
                    handleFailedFetch("Échec de la récupération des rendez-vous : ${response.message()}")
                }
            } catch (e: Exception) {
                handleFailedFetch("Erreur lors de la récupération des rendez-vous : ${e.message}")
            } finally {
                isRefreshing = false
                binding.progressBar.visibility = View.GONE
                binding.recyclerViewAppointments.visibility = View.VISIBLE
            }
        }
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = SwipeGestureCallback(
            onDelete = { position ->
                val appointment = appointmentAdapter.removeItem(position)
                showDeleteConfirmationDialog(appointment, position)
            },
            swipeDirections = SwipeGestureCallback.SWIPE_RIGHT
        )

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewAppointments)
    }

    private fun showDeleteConfirmationDialog(appointment: Appointment, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Annuler le rendez-vous")
            .setMessage("Voulez-vous vraiment annuler ce rendez-vous ?")
            .setPositiveButton("Oui") { _, _ ->
                if (appointment.statut) {
                    // Show toast if appointment is already accepted
                    appointmentAdapter.restoreItem(appointment, position)
                    Toast.makeText(requireContext(), "Rendez-vous déjà accepté, suppression impossible", Toast.LENGTH_SHORT).show()

                } else {
                    // Delete the appointment if statut is false
                    deleteAppointment(appointment.doctorId, appointment.date)
                }
            }
            .setNegativeButton("Non") { _, _ ->
                appointmentAdapter.restoreItem(appointment, position)
            }
            .setCancelable(false)
            .show()
    }


    private fun deleteAppointment(doctorId: Int, date: String) {
        lifecycleScope.launch {
            val result = appointmentRepository.deleteAppointment(doctorId, date)
            result.onSuccess {
                showSuccessDialog("Rendez-vous annulé avec succès")
                fetchAppointments()
            }.onFailure { error ->
                showErrorDialog(error.message ?: "Erreur lors de l'annulation")
                fetchAppointments()
            }
        }
    }

    private fun fetchAppointments() {
        lifecycleScope.launch {
            val result = appointmentRepository.fetchAppointmentsByPatientId(currentPatientId)
            result.onSuccess { fetchedAppointments ->
                handleSuccessfulFetch(fetchedAppointments)
            }.onFailure { error ->
                handleFailedFetch(error.message ?: "Failed to load appointments")
            }
        }
    }

    private fun handleSuccessfulFetch(fetchedAppointments: List<Appointment>) {
        appointments = fetchedAppointments.toMutableList()
        filterAndSortAppointments()

        if (fetchedAppointments.isNotEmpty()) {
            binding.recyclerViewAppointments.visibility = View.VISIBLE
        } else {
            if (!isRefreshing) {
                binding.recyclerViewAppointments.visibility = View.GONE
                Log.i("HomeDoc", "Aucun rendez-vous trouvé.")
            }
        }
    }

    private fun handleFailedFetch(errorMessage: String) {
        Log.e("Appointments", errorMessage)
        showErrorDialog("Erreur lors du chargement des rendez-vous")
    }

    private fun showUpdateAppointmentDialog(appointment: Appointment) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.appointment_dialog)

        // Updated component references
        val selectDateButton = dialog.findViewById<Button>(R.id.selectDateButton)
        val timeSlotRecyclerView = dialog.findViewById<RecyclerView>(R.id.timeSlotRecyclerView) // New
        val confirmButton = dialog.findViewById<Button>(R.id.confirmButton)
        val selectedDateText = dialog.findViewById<TextView>(R.id.selectedDateText)

        // Initialize with current appointment data
        setupDialogInitialState(appointment, selectedDateText)

        // Setup time slot selection
        setupTimeSlotRecyclerView(timeSlotRecyclerView, appointment) { selectedTime ->
            confirmButton.isEnabled = true
        }

        selectDateButton.setOnClickListener {
            showDatePicker(selectedDateText, appointment, timeSlotRecyclerView)
        }

        var selectedTime: String? = null
        timeSlotRecyclerView.adapter = (timeSlotRecyclerView.adapter as? TimeSlotAdapter)?.apply {
            setOnTimeSlotClickListener { time ->
                selectedTime = time
                confirmButton.isEnabled = true
            }
        }

        confirmButton.setOnClickListener {
            selectedTime?.let { time ->
                handleUpdateConfirmation(dialog, appointment, selectedDateText, time)
            }
        }

        dialog.show()
    }

    private fun setupDialogInitialState(appointment: Appointment, selectedDateText: TextView) {
        val (date, time) = appointment.date.split("T")
        val (year, month, day) = date.split("-")
        selectedDateText.text = "$day/$month/$year"
    }

    private fun setupTimeSlotRecyclerView(
        recyclerView: RecyclerView,
        appointment: Appointment,
        onTimeSelected: (String) -> Unit
    ) {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val timeSlots = listOf("09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00")
        val adapter = TimeSlotAdapter(timeSlots,0)
        recyclerView.adapter = adapter

        // Initially hide the RecyclerView
        recyclerView.visibility = View.GONE

        adapter.setOnTimeSlotClickListener { time ->
            onTimeSelected(time)
        }
    }


    private fun showDatePicker(
        selectedDateText: TextView,
        currentAppointment: Appointment,
        timeSlotRecyclerView: RecyclerView
    ) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = "$dayOfMonth/${month + 1}/$year"
                selectedDateText.text = selectedDate
                timeSlotRecyclerView.visibility = View.VISIBLE
                fetchAppointmentsForDate(currentAppointment.doctorId, selectedDate, timeSlotRecyclerView)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    private fun fetchAppointmentsForDate(doctorId: Int, selectedDate: String, recyclerView: RecyclerView) {
        lifecycleScope.launch {
            try {
                val result = appointmentRepository.fetchAppointmentsByDoctorId(doctorId)
                result.onSuccess { appointments ->
                    val filteredAppointments = filterAppointmentsByDate(appointments, selectedDate)
                    (recyclerView.adapter as? TimeSlotAdapter)?.updateAppointments(filteredAppointments)
                }.onFailure {
                    showErrorDialog("Erreur lors de la récupération des rendez-vous")
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

    private fun handleUpdateConfirmation(
        dialog: Dialog,
        appointment: Appointment,
        selectedDateText: TextView,
        selectedTime: String
    ) {
        val selectedDate = selectedDateText.text.toString()
        if (selectedDate != "Date sélectionnée") {
            updateAppointment(appointment, selectedDate, selectedTime)
            dialog.dismiss()
        }
    }

    private fun updateAppointment(appointment: Appointment, date: String, hour: String) {
        val dateTimeStr = formatDateTime(date, hour)
        val request = AppointmentRequest(
            date = dateTimeStr,
            patientId = currentPatientId,
            doctorId = appointment.doctorId,
            statut = appointment.statut
        )

        lifecycleScope.launch {
            val result = appointmentRepository.updateAppointment(appointment.doctorId, appointment.date, request)
            result.onSuccess {
                showSuccessDialog("Rendez-vous modifié pour:\nDate: $date\nHeure: $hour")
                fetchAppointments()
            }.onFailure { error ->
                showErrorDialog(error.message ?: "Erreur lors de la mise à jour")
            }
        }
    }

    private fun formatDateTime(date: String, hour: String): String {
        val (day, month, year) = date.split("/")
        return "$year-${month.padStart(2, '0')}-${day.padStart(2, '0')}T$hour:00"
    }

    private fun showSuccessDialog(message: String) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.confirmation_dialog)

        dialog.findViewById<TextView>(R.id.appointmentDetails).text = message
        dialog.findViewById<Button>(R.id.okButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showErrorDialog(message: String) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.error_dialog)

        dialog.findViewById<TextView>(R.id.errorMessage).text = message
        dialog.findViewById<Button>(R.id.okButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}