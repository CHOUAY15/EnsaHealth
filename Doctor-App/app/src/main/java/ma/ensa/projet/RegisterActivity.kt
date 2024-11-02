package ma.ensa.projet

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import ma.ensa.projet.api.repository.DoctorRepository

import ma.ensa.projet.repository.PatientRepository
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private val TAG = "DoctorInscription"

    private val doctorRepository = DoctorRepository()
    private val patientRepository = PatientRepository()


    private lateinit var rgUserType: RadioGroup
    private lateinit var layoutDoctorFields: LinearLayout
    private lateinit var layoutPatientFields: LinearLayout
    private lateinit var btnRegister: MaterialButton
    private lateinit var btnAddPhoto: MaterialButton
    private lateinit var ivProfileImage: ImageView
    private lateinit var btnBack: ImageView


    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var spinnerSpecialty: AutoCompleteTextView
    private lateinit var etAddress: TextInputEditText
    private lateinit var etExperience: TextInputEditText
    private lateinit var etBiography: TextInputEditText


    private var selectedImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                ivProfileImage.setImageURI(uri)
                ivProfileImage.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initializeViews()
        setupListeners()
        setupSpecialtyDropdown()
    }

    private fun initializeViews() {
        rgUserType = findViewById(R.id.rgUserType)
        layoutDoctorFields = findViewById(R.id.layoutDoctorFields)
        layoutPatientFields = findViewById(R.id.layoutPatientFields)
        btnRegister = findViewById(R.id.btnRegister)
        btnAddPhoto = findViewById(R.id.btnAddPhoto)
        ivProfileImage = findViewById(R.id.ivProfileImage)
        btnBack = findViewById(R.id.btnnBack)
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etPhone = findViewById(R.id.etPhone)
        spinnerSpecialty = findViewById(R.id.spinnerSpecialty)
        etAddress = findViewById(R.id.etAddress)
        etExperience = findViewById(R.id.etExperience)
        etBiography = findViewById(R.id.etBiography)
    }

    private fun setupListeners() {
        rgUserType.setOnCheckedChangeListener { _, checkedId ->
            layoutDoctorFields.visibility = if (checkedId == R.id.rbDoctor) View.VISIBLE else View.GONE
            layoutPatientFields.visibility = if (checkedId == R.id.rbPatient) View.VISIBLE else View.GONE
        }

        btnBack.setOnClickListener {
            Log.d("click","oui")
            finish()
        }

        btnAddPhoto.setOnClickListener {
            openImagePicker()
        }

        btnRegister.setOnClickListener {
            if (validateFields()) {
                if (rgUserType.checkedRadioButtonId == R.id.rbDoctor) {
                    registerDoctor()
                } else {
                    registerPatient()
                }
            }
        }
    }

    private fun setupSpecialtyDropdown() {
        val specialties = arrayOf(
            "Psychiatrie",
            "Pédiatrie",
            "Orthopédie",
            "Neurologie",
            "Dentisterie",
            "Cardiologie"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, specialties)
        spinnerSpecialty.setAdapter(adapter)
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImage.launch(intent)
    }

    private fun validateFields(): Boolean {
        if (etFirstName.text.isNullOrEmpty() ||
            etLastName.text.isNullOrEmpty() ||
            etEmail.text.isNullOrEmpty() ||
            etPassword.text.isNullOrEmpty()
        ) {
            showError("Veuillez remplir tous les champs obligatoires")
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString()).matches()) {
            showError("S'il vous plaît, mettez une adresse email valide")
            return false
        }

        if (etPassword.text.toString().length < 6) {
            showError("Le mot de passe doit comporter au moins 6 caractères")
            return false
        }

        if (rgUserType.checkedRadioButtonId == R.id.rbDoctor) {
            if (spinnerSpecialty.text.isNullOrEmpty() ||
                etAddress.text.isNullOrEmpty() ||
                etExperience.text.isNullOrEmpty() ||
                etBiography.text.isNullOrEmpty()
            ) {
                showError("Veuillez remplir tous les champs spécifiques au médecin")
                return false
            }

            try {
                val experience = etExperience.text.toString().toInt()
                if (experience < 0 || experience > 60) {
                    showError("Veuillez entrer un nombre valide d'années d'expérience")
                    return false
                }
            } catch (e: NumberFormatException) {
                showError("Veuillez entrer un numéro valide pour les années d'expérience")
                return false
            }

            if (selectedImageUri == null) {
                showError("Veuillez sélectionner une photo de profil")
                return false
            }
        }

        if (rgUserType.checkedRadioButtonId == R.id.rbPatient) {
            if (etPhone.text.isNullOrEmpty()) {
                showError("Veuillez entrer un numéro de téléphone")
                return false
            }

            if (!android.util.Patterns.PHONE.matcher(etPhone.text.toString()).matches()) {
                showError("Veuillez entrer un numéro de téléphone valide")
                return false
            }
        }

        return true
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun registerDoctor() {
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()
        val firstName = etFirstName.text.toString()
        val lastName = etLastName.text.toString()
        val specialty = spinnerSpecialty.text.toString()
        val address = etAddress.text.toString()
        val experience = etExperience.text.toString()
        val biography = etBiography.text.toString()

        val call = doctorRepository.registerDoctor(
            email,
            password,
            firstName,
            lastName,
            specialty,
            address,
            experience,
            biography,
            selectedImageUri,
            selectedImageUri?.let { getPathFromUri(it) }
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val message = response.body()?.string()
                    Log.d(TAG, "onResponse: Success - $message")
                    clearInputs()
                    Toast.makeText(this@RegisterActivity, message ?: "Inscription réussie", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG, "onResponse: Failure - ${response.errorBody()?.string()}")
                    Toast.makeText(this@RegisterActivity, "L'inscription a échoué", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
                Toast.makeText(this@RegisterActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun registerPatient() {
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()
        val firstName = etFirstName.text.toString()
        val lastName = etLastName.text.toString()
        val phoneNumber = etPhone.text.toString()

        val call = patientRepository.registerPatient(
            email,
            password,
            firstName,
            lastName,
            phoneNumber
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val message = response.body()?.string()
                    Log.d(TAG, "onResponse: Success - $message")
                    clearInputs()
                    Toast.makeText(this@RegisterActivity, message ?: "Inscription réussie", Toast.LENGTH_SHORT).show()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "onResponse: Failure - $errorBody")
                    Toast.makeText(this@RegisterActivity, errorBody ?: "L'inscription a échoué", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
                Toast.makeText(this@RegisterActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getPathFromUri(uri: Uri): String {
        var path = ""
        val cursor = contentResolver.query(uri, null, null, null, null)
        if (cursor != null) {
            cursor.moveToFirst()
            val index = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
            path = cursor.getString(index)
            cursor.close()
        }
        return path
    }



    private fun clearInputs() {
        etFirstName.text?.clear()
        etLastName.text?.clear()
        etEmail.text?.clear()
        etPassword.text?.clear()

        spinnerSpecialty.text?.clear()
        etAddress.text?.clear()
        etExperience.text?.clear()
        etBiography.text?.clear()

        etPhone.text?.clear()

        ivProfileImage.setImageDrawable(null)
        selectedImageUri = null

        ivProfileImage.visibility = View.GONE
    }
}