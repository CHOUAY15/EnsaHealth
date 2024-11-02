package ma.ensa.projet.ui.patient

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import ma.ensa.projet.AuthActivity
import ma.ensa.projet.AuthManager
import ma.ensa.projet.R
import ma.ensa.projet.databinding.FragmentProfilBinding
import ma.ensa.projet.repository.PatientRepository
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfilFragment : Fragment() {
    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!
    private var isEditMode = false
    private lateinit var authManager: AuthManager
    private val patientRepository = PatientRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        authManager = AuthManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupViews()
        loadProfileData()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            title = "Mon Profil"
            inflateMenu(R.menu.profile_menu)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
                        toggleEditMode()
                        true
                    }
                    R.id.Logout -> {
                        logout()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun logout() {

        authManager.logout()


        val intent = Intent(requireContext(), AuthActivity::class.java) // Make sure to replace AuthActivity with the actual name of your authentication activity
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear the task stack
        startActivity(intent)
        requireActivity().finish() // Finish the current activity
    }

    private fun setupViews() {
        binding.apply {
            editTextFirstName.isEnabled = false
            editTextLastName.isEnabled = false
            editTextPhone.isEnabled = false


            buttonSave.setOnClickListener { saveChanges() }
            buttonCancel.setOnClickListener { cancelEdit() }
        }
    }

    private fun loadProfileData() {
        authManager.getUser()?.let { user ->
            binding.apply {
                editTextFirstName.setText(user.firstName)
                editTextLastName.setText(user.lastName)
                editTextPhone.setText(user.phoneNumber)
            }
        }
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        binding.apply {
            editTextFirstName.isEnabled = isEditMode
            editTextLastName.isEnabled = isEditMode
            editTextPhone.isEnabled = isEditMode
            buttonSave.isVisible = isEditMode
            buttonCancel.isVisible = isEditMode
        }
    }

    private fun saveChanges() {
        val userId = authManager.getUserId()
        binding.buttonSave.isEnabled = false

        patientRepository.updatePatient(
            id = userId,
            firstName = binding.editTextFirstName.text.toString(),
            lastName = binding.editTextLastName.text.toString(),
            phoneNumber = binding.editTextPhone.text.toString()
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (isAdded) {
                    binding.buttonSave.isEnabled = true
                    if (response.isSuccessful) {
                        authManager.getUser()?.let { currentUser ->
                            authManager.updateUserData(
                                currentUser.copy(
                                    firstName = binding.editTextFirstName.text.toString(),
                                    lastName = binding.editTextLastName.text.toString(),
                                    phoneNumber = binding.editTextPhone.text.toString()
                                )
                            )
                        }
                        toggleEditMode()
                        Snackbar.make(binding.root, "Profil mis à jour avec succès", Snackbar.LENGTH_SHORT).show()
                    } else {
                        Snackbar.make(binding.root, "Erreur lors de la mise à jour", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                if (isAdded) {
                    binding.buttonSave.isEnabled = true
                    Snackbar.make(binding.root, "Erreur réseau", Snackbar.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun cancelEdit() {
        loadProfileData()
        toggleEditMode()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
