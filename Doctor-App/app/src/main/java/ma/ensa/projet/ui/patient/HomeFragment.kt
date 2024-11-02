package ma.ensa.projet.ui.patient

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ma.ensa.projet.DoctorDetaillActivity
import ma.ensa.projet.ListDoctorActivity
import ma.ensa.projet.R
import ma.ensa.projet.adapter.DoctorHAdapter
import ma.ensa.projet.adapter.SpecialityAdapter
import ma.ensa.projet.api.repository.DoctorRepository
import ma.ensa.projet.beans.Doctor
import ma.ensa.projet.beans.Speciality
import ma.ensa.projet.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val doctorRepository = DoctorRepository()
    private var doctorList: List<Doctor> = listOf()
    private var isFragmentActive = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isFragmentActive = true

        loadUserData()
        setupSpecialities()
        setupSeeAllButton()
        fetchDoctors()
    }

    private fun setupSpecialities() {
        val specialities = listOf(
            Speciality("Cardiologie", R.drawable.cardiologie),
            Speciality("Dentisterie", R.drawable.dentisterie),
            Speciality("Neurologie", R.drawable.neurologie),
            Speciality("Orthopédie", R.drawable.orthopedie),
            Speciality("Pédiatrie", R.drawable.pediatrie),
            Speciality("Psychiatrie", R.drawable.psychiatrie)
        )

        _binding?.let { safeBinding ->
            val specialityAdapter = SpecialityAdapter(specialities, requireContext())
            safeBinding.rvSpecialities.apply {
                adapter = specialityAdapter
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            }
        }
    }

    private fun setupSeeAllButton() {
        _binding?.seeAll?.setOnClickListener {
            val intent = Intent(requireContext(), ListDoctorActivity::class.java)
            intent.putExtra("DOCTORS_LIST", ArrayList(doctorList))
            startActivity(intent)
        }
    }

    private fun loadUserData() {
        _binding?.let { safeBinding ->
            val sharedPrefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            val firstName = sharedPrefs.getString("first_name", "User")
            val lastName = sharedPrefs.getString("last_name", "")
            val greetingText = "Salut, $firstName $lastName"

            Log.d("UserPrefs", "First Name: $firstName")
            Log.d("UserPrefs", "Last Name: $lastName")

            safeBinding.tvGreeting.text = greetingText
        }
    }

    private fun fetchDoctors() {
        doctorRepository.fetchDoctors(
            onDoctorsFetched = { doctors ->
                if (isFragmentActive) {
                    doctorList = doctors
                    setupDoctorRecyclerView(doctors)
                }
            },
            onError = { throwable ->
                if (isFragmentActive) {
                    Log.e("HomeFragment", "Error fetching doctors", throwable)
                }
            }
        )
    }

    private fun setupDoctorRecyclerView(doctors: List<Doctor>) {
        _binding?.let { safeBinding ->
            val doctorAdapter = DoctorHAdapter(doctors, showAll = false) { doctor ->
                val intent = Intent(requireContext(), DoctorDetaillActivity::class.java)
                intent.putExtra("selected_doctor", doctor)
                startActivity(intent)
            }

            safeBinding.rvDoctors.apply {
                adapter = doctorAdapter
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            }
        }
    }

    override fun onDestroyView() {
        isFragmentActive = false
        _binding = null
        super.onDestroyView()
    }
}
