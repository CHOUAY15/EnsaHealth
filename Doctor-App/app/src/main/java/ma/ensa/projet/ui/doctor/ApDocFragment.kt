package ma.ensa.projet.ui.doctor

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ma.ensa.projet.AuthManager
import ma.ensa.projet.R
import ma.ensa.projet.adapter.AppointmentDocAdapter
import ma.ensa.projet.api.data.AppointmentRequest
import ma.ensa.projet.api.repository.AppointmentRepository
import ma.ensa.projet.beans.Appointment
import ma.ensa.projet.databinding.FragmentAppoinDocBinding
import ma.ensa.projet.util.SwipeGestureCallback

class ApDocFragment : Fragment() {
    private var _binding: FragmentAppoinDocBinding? = null
    private val binding get() = _binding!!
    private lateinit var authManager: AuthManager
    private lateinit var appointmentRepository: AppointmentRepository
    private var currentDoctorId: Int = 0
    private lateinit var appointmentAdapter: AppointmentDocAdapter
    private var appointments = mutableListOf<Appointment>()
    private var isAscendingOrder = true
    private var currentFilter = "ALL"
    private var currentSearchQuery = ""
    private var isRefreshing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAppoinDocBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        setupInitialState()
        setupFilterChips()
        fetchAppointments()
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

    private fun refreshAppointments() {
        isRefreshing = true
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerViewAppointments.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = appointmentRepository.fetchAppointmentsByDoctorId(currentDoctorId)
                delay(2000)
                response.onSuccess { fetchedAppointments ->
                    handleSuccessfulFetch(fetchedAppointments)
                }.onFailure { error ->
                    handleFailedFetch(error.message ?: "Failed to load appointments")
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

    private fun setupInitialState() {
        authManager = AuthManager(requireContext())
        currentDoctorId = authManager.getToken()?.let { authManager.getUserId() } ?: 0
        appointmentRepository = AppointmentRepository(requireContext())
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        appointmentAdapter = AppointmentDocAdapter(appointments) { appointment -> }
        setupRecyclerView()
        setupSwipeToDelete()
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
            val matchesSearch = if (currentSearchQuery.isEmpty()) true
            else appointment.ptienName.contains(currentSearchQuery, ignoreCase = true)

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

    private fun setupRecyclerView() {
        binding.recyclerViewAppointments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = appointmentAdapter
            setHasFixedSize(true)
        }
    }

    private fun fetchAppointments() {
        lifecycleScope.launch {
            val result = appointmentRepository.fetchAppointmentsByDoctorId(currentDoctorId)
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
        Log.e("HomeDoc", errorMessage)
        if (!isRefreshing) {
            binding.recyclerViewAppointments.visibility = View.GONE
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = SwipeGestureCallback(
            onDelete = { position ->
                val appointment = appointmentAdapter.removeItem(position)
                showDeleteConfirmationDialog(appointment, position)
            },
            onStatusUpdate = { position ->
                val appointment = appointments[position]
                updateAppointment(appointment)
            },
            swipeDirections = SwipeGestureCallback.SWIPE_BOTH
        )
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewAppointments)
    }

    private fun updateAppointment(appointment: Appointment) {
        val request = AppointmentRequest(
            date = appointment.date,
            patientId = appointment.patientId,
            doctorId = currentDoctorId,
            statut = true
        )

        lifecycleScope.launch {
            val result = appointmentRepository.updateAppointment(appointment.doctorId, appointment.date, request)
            result.onSuccess {
                showSuccessDialog("Rendez-vous confirme")
                fetchAppointments()
            }.onFailure { error ->
                showErrorDialog(error.message ?: "Erreur lors de la mise à jour")
            }
        }
    }

    private fun showDeleteConfirmationDialog(appointment: Appointment, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Annuler le rendez-vous")
            .setMessage("Voulez-vous vraiment annuler ce rendez-vous ?")
            .setPositiveButton("Oui") { _, _ ->
                deleteAppointment(appointment.doctorId, appointment.date)
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