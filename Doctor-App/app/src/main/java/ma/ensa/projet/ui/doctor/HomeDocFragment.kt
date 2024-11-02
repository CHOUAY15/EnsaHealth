package ma.ensa.projet.ui.doctor

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import ma.ensa.projet.AuthManager
import ma.ensa.projet.adapter.AppointmentDocAdapter
import ma.ensa.projet.api.RetrofitClient
import ma.ensa.projet.api.repository.AppointmentRepository
import ma.ensa.projet.beans.Appointment
import ma.ensa.projet.databinding.FragmentHomeDocBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeDocFragment : Fragment() {
    private var _binding: FragmentHomeDocBinding? = null
    private val binding get() = _binding!!
    private lateinit var authManager: AuthManager
    private lateinit var appointmentRepository: AppointmentRepository
    private var currentDoctorId: Int = 0
    private lateinit var appointmentAdapter: AppointmentDocAdapter
    private var appointments = mutableListOf<Appointment>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeDocBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupInitialState()
        fetchAppointments()
    }

    private fun setupInitialState() {
        appointmentRepository = AppointmentRepository(requireContext())
        initializeManagers()
        setupRecyclerView()
        setupDoctorGreeting()
    }

    private fun initializeManagers() {
        authManager = AuthManager(requireContext())
        currentDoctorId = authManager.getToken()?.let { authManager.getUserId() } ?: 0
        appointmentAdapter = AppointmentDocAdapter(appointments) { appointment -> }
    }

    private fun setupDoctorGreeting() {
        val doctorName = authManager.getToken()?.let { authManager.getUserName() } ?: "Docteur"
        val doctorSpe = authManager.getToken()?.let { authManager.getDocSpec() } ?: "spe"

        binding.apply {
            tvGreetingDoc.text = "Salut,Dr  $doctorName"
            tvSubGreeting.text = "Spécialité  $doctorSpe"
        }
    }

    private fun setupRecyclerView() {
        binding.rvAppointments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = appointmentAdapter
            setHasFixedSize(true)
        }
    }

    private fun fetchAppointments() {
        lifecycleScope.launch {
            val result = appointmentRepository.fetchAppointmentsByDoctorId(currentDoctorId)
            result.onSuccess { fetchedAppointments ->
                handleSuccessfulFetch(filterTodayAppointments(fetchedAppointments))
            }.onFailure { error ->
                handleFailedFetch(error.message ?: "Failed to load appointments")
            }
        }
    }

    private fun filterTodayAppointments(appointments: List<Appointment>?): List<Appointment> {
        Log.d("AppointmentHomeDoc", "$appointments")
        if (appointments == null) return emptyList()

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return appointments.filter { appointment ->
            try {
                val appointmentDate = parseAppointmentDate(appointment.date)
                appointment.statut && appointmentDate == today
            } catch (e: Exception) {
                Log.e("AppointmentHomeDoc", "Error parsing date: ${e.message}")
                false
            }
        }
    }

    private fun parseAppointmentDate(dateString: String): String? {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            .parse(dateString)?.let {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
            }
    }

    private fun handleSuccessfulFetch(fetchedAppointments: List<Appointment>) {
        binding.apply {
            if (fetchedAppointments.isNotEmpty()) {
                // Show RecyclerView, hide ImageView for empty state
                rvAppointments.visibility = View.VISIBLE
                ivEmptyState.visibility = View.GONE
                (rvAppointments.adapter as? AppointmentDocAdapter)?.updateAppointments(fetchedAppointments)
            } else {
                // Hide RecyclerView and show the empty state ImageView
                rvAppointments.visibility = View.GONE
                ivEmptyState.visibility = View.VISIBLE
                Log.i("HomeDoc", "Aucun rendez-vous trouvé pour aujourd'hui.")
            }
        }
    }



    private fun handleFailedFetch(errorMessage: String) {
        Log.e("HomeDoc", errorMessage)
        binding.rvAppointments.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}