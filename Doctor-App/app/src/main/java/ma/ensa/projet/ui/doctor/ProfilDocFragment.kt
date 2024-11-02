package ma.ensa.projet.ui.doctor

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import ma.ensa.projet.AuthActivity
import ma.ensa.projet.R
import ma.ensa.projet.databinding.FragmentProfilDocBinding
import ma.ensa.projet.api.RetrofitClient
import ma.ensa.projet.AuthManager
import ma.ensa.projet.api.data.Person

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File





class ProfilDocFragment : Fragment() {
    private var _binding: FragmentProfilDocBinding? = null
    private val binding get() = _binding!!
    private var isEditMode = false
    private var currentImageUri: Uri? = null
    private val apiService = RetrofitClient.apiService
    private lateinit var authManager: AuthManager
    private var currentPersonDto: Person? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            currentImageUri = it
            loadImage(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilDocBinding.inflate(inflater, container, false)
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
        // Clear the user data
        authManager.logout()

        // Navigate back to Auth Activity
        val intent = Intent(requireContext(), AuthActivity::class.java) // Make sure to replace AuthActivity with the actual name of your authentication activity
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear the task stack
        startActivity(intent)
        requireActivity().finish() // Finish the current activity
    }

    private fun setupViews() {
        binding.apply {
            // Initially disable all edit texts
            editTextFirstName.isEnabled = false
            editTextLastName.isEnabled = false
            editTextPhone.isEnabled = false
            editTextSpecialty.isEnabled = false
            editTextAddress.isEnabled = false
            editTextBiography.isEnabled = false
            editTextExperience.isEnabled = false

            profileImage.setOnClickListener {
                if (isEditMode) {
                    pickImage.launch("image/*")
                }
            }

            buttonSave.setOnClickListener {
                showConfirmationDialog()
            }

            buttonCancel.setOnClickListener {
                cancelEdit()
            }
        }
    }

    private fun loadProfileData() {
        try {
            authManager.getUser()?.let { user ->
                currentPersonDto = user
                updateUIWithPersonDto(user)
            } ?: run {
                showError("Unable to load user data. Please login again.")
                // Optionally handle the case where user data is not available
                // For example, redirect to login screen
            }
        } catch (e: Exception) {
            showError("Error loading profile: ${e.message}")
        }
    }

    private fun updateUIWithPersonDto(person: Person) {
        binding.apply {
            editTextFirstName.setText(person.firstName)
            editTextLastName.setText(person.lastName)
            editTextPhone.setText(person.phoneNumber)
            editTextSpecialty.setText(person.specialty)
            editTextAddress.setText(person.address)
            editTextExperience.setText(person.yearsExperience?.toString())
            editTextBiography.setText(person.biography)

            loadImage(person.getFullImageUrl())
        }
    }

    private fun loadImage(imageUri: Any) {
        Glide.with(requireContext())
            .load(imageUri)
//            .placeholder(R.drawable.default_profile)
//            .error(R.drawable.default_profile)
            .circleCrop()
            .into(binding.profileImage)
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        binding.apply {
            editTextFirstName.isEnabled = isEditMode
            editTextLastName.isEnabled = isEditMode
            editTextPhone.isEnabled = isEditMode
            editTextSpecialty.isEnabled = isEditMode
            editTextAddress.isEnabled = isEditMode
            editTextBiography.isEnabled = isEditMode
            editTextExperience.isEnabled = isEditMode

            buttonSave.isVisible = isEditMode
            buttonCancel.isVisible = isEditMode
            toolbar.menu.findItem(R.id.action_edit)?.isVisible = !isEditMode
        }
    }

    private fun showConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmer la modification")
            .setMessage("Voulez-vous vraiment enregistrer ces modifications ?")
            .setPositiveButton("Oui") { _, _ -> saveChanges() }
            .setNegativeButton("Non", null)
            .show()
    }

    private fun saveChanges() {
        try {
            val userId = authManager.getUserId()

            if (!hasDataChanged()) {
                showMessage("Aucune modification n'a été effectuée")
                toggleEditMode()
                return
            }

            // Create RequestBody instances for text fields using plain text media type
            val createTextPart = { text: String ->
                RequestBody.create("text/plain".toMediaTypeOrNull(), text)
            }

            val updates = mapOf(
                "firstName" to createTextPart(binding.editTextFirstName.text.toString()),
                "lastName" to createTextPart(binding.editTextLastName.text.toString()),
                "phone" to createTextPart(binding.editTextPhone.text.toString()),
                "specialty" to createTextPart(binding.editTextSpecialty.text.toString()),
                "address" to createTextPart(binding.editTextAddress.text.toString()),
                "biography" to createTextPart(binding.editTextBiography.text.toString()),
                "yearsExperience" to createTextPart(binding.editTextExperience.text.toString())
            )

            // Handle image
            var imagePart: MultipartBody.Part? = null
            currentImageUri?.let { uri ->
                val file = File(getRealPathFromUri(uri))
                if (file.exists()) {
                    val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
                    imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
                }
            }

            apiService.updateDoctor(
                id = userId,
                firstName = updates["firstName"]!!,
                lastName = updates["lastName"]!!,
                phone = updates["phone"]!!,
                specialty = updates["specialty"]!!,
                address = updates["address"]!!,
                biography = updates["biography"]!!,
                yearsExperience = updates["yearsExperience"]!!,
                image = imagePart
            ).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {

                        handleSuccessfulUpdate()
                    } else {
                        try {
                            val errorBody = response.errorBody()?.string()
                            showError("Échec de la mise à jour: ${response.code()} - $errorBody")
                        } catch (e: Exception) {
                            showError("Échec de la mise à jour: ${response.code()}")
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    showError("Erreur réseau: ${t.message}")
                }
            })

        } catch (e: Exception) {
            showError("Erreur: ${e.message}")
        }
    }

    private fun handleSuccessfulUpdate() {
        val updatedPerson = Person(
            id = authManager.getUserId(),
            firstName = binding.editTextFirstName.text.toString(),
            lastName = binding.editTextLastName.text.toString(),
            phoneNumber = binding.editTextPhone.text.toString(),
            specialty = binding.editTextSpecialty.text.toString(),
            address = binding.editTextAddress.text.toString(),
            biography = binding.editTextBiography.text.toString(),
            yearsExperience = binding.editTextExperience.text.toString().toIntOrNull(),
            image = currentPersonDto?.image
        )

        // Update SharedPreferences
        currentPersonDto = updatedPerson
        authManager.updateUserData(updatedPerson)


        showSuccessDialog()
    }

    private fun hasDataChanged(): Boolean {
        return currentPersonDto?.let { oldData ->
            binding.run {
                oldData.firstName != editTextFirstName.text.toString() ||
                        oldData.lastName != editTextLastName.text.toString() ||
                        oldData.phoneNumber != editTextPhone.text.toString() ||
                        oldData.specialty != editTextSpecialty.text.toString() ||
                        oldData.address != editTextAddress.text.toString() ||
                        oldData.biography != editTextBiography.text.toString() ||
                        oldData.yearsExperience?.toString() != editTextExperience.text.toString() ||
                        currentImageUri != null
            }
        } ?: false
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Succès")
            .setMessage("Votre profil a été mis à jour avec succès")
            .setPositiveButton("OK") { _, _ ->
                activity?.runOnUiThread {
                    toggleEditMode()
                    loadProfileData() // Reload the updated data
                }
            }
            .show()
    }

    private fun getRealPathFromUri(uri: Uri): String {
        var path = ""
        context?.contentResolver?.let { resolver ->
            val cursor = resolver.query(uri, null, null, null, null)
            cursor?.use {
                it.moveToFirst()
                val index = it.getColumnIndex(MediaStore.Images.Media.DATA)
                path = it.getString(index)
            }
        }
        return path
    }

    private fun showError(message: String) {
        activity?.runOnUiThread {
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun showMessage(message: String) {
        activity?.runOnUiThread {
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun cancelEdit() {
        currentPersonDto?.let { updateUIWithPersonDto(it) }
        toggleEditMode()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}