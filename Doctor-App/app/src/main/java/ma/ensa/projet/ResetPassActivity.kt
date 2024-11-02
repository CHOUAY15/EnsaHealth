package ma.ensa.projet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResetPassActivity : AppCompatActivity() {
    private lateinit var authManager: AuthManager

    // Views
    private lateinit var emailSection: LinearLayout
    private lateinit var resetSection: LinearLayout
    private lateinit var progressIndicator: CircularProgressIndicator

    // Email section views
    private lateinit var emailInput: TextInputEditText
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var sendCodeButton: MaterialButton

    // Reset section views
    private lateinit var codeInput: TextInputEditText
    private lateinit var codeInputLayout: TextInputLayout
    private lateinit var newPasswordInput: TextInputEditText
    private lateinit var newPasswordInputLayout: TextInputLayout
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var confirmPasswordInputLayout: TextInputLayout
    private lateinit var resetPasswordButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reset_pass)

        // Initialize AuthManager
        authManager = AuthManager(this)

        // Initialize views
        initializeViews()
        setupWindowInsets()
        setupClickListeners()
    }

    private fun initializeViews() {
        // Sections
        emailSection = findViewById(R.id.emailSection)
        resetSection = findViewById(R.id.resetSection)
        progressIndicator = findViewById(R.id.progressIndicator)

        // Email section
        emailInput = findViewById(R.id.emailInput)
        emailInputLayout = findViewById(R.id.emailInputLayout)
        sendCodeButton = findViewById(R.id.sendCodeButton)

        // Reset section
        codeInput = findViewById(R.id.codeInput)
        codeInputLayout = findViewById(R.id.codeInputLayout)
        newPasswordInput = findViewById(R.id.newPasswordInput)
        newPasswordInputLayout = findViewById(R.id.newPasswordInputLayout)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout)
        resetPasswordButton = findViewById(R.id.resetPasswordButton)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupClickListeners() {
        sendCodeButton.setOnClickListener {
            handleSendCodeClick()
        }

        resetPasswordButton.setOnClickListener {
            handleResetPasswordClick()
        }
    }

    private fun handleSendCodeClick() {
        val email = emailInput.text.toString().trim()

        // Reset previous errors
        emailInputLayout.error = null

        // Validate email
        if (email.isEmpty()) {
            emailInputLayout.error = "Email is required"
            return
        }

        if (!isValidEmail(email)) {
            emailInputLayout.error = "Please enter a valid email address"
            return
        }

        // Show loading state
        setLoadingState(true)

        lifecycleScope.launch {
            try {
                authManager.requestPasswordReset(email)
                // Success
                showToast("Code de réinitialisation envoyé à votre email")
                switchToResetSection()
            } catch (e: Exception) {
                // Error
                emailInputLayout.error = e.message
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun handleResetPasswordClick() {
        val email = emailInput.text.toString().trim()
        val code = codeInput.text.toString().trim()
        val newPassword = newPasswordInput.text.toString()
        val confirmPassword = confirmPasswordInput.text.toString()

        // Reset previous errors
        codeInputLayout.error = null
        newPasswordInputLayout.error = null
        confirmPasswordInputLayout.error = null

        // Validate inputs
        if (code.isEmpty()) {
            codeInputLayout.error = "Verification code is required"
            return
        }

        if (newPassword.isEmpty()) {
            newPasswordInputLayout.error = "New password is required"
            return
        }

        if (newPassword.length < 6) {
            newPasswordInputLayout.error = "Password must be at least 8 characters"
            return
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordInputLayout.error = "Please confirm your password"
            return
        }

        if (newPassword != confirmPassword) {
            confirmPasswordInputLayout.error = "Les mots de passe ne correspondent pas"
            return
        }

        // Show loading state
        setLoadingState(true)

        lifecycleScope.launch {
            try {
                authManager.confirmPasswordReset(email, code, newPassword)
                // Success
                showToast("Réinitialisation du mot de passe réussie")

                loginUser(email, newPassword)


            } catch (e: Exception) {
                // Error
                showToast(e.message ?: "Password reset failed")
            } finally {
                setLoadingState(false)
            }
        }
    }
    private fun loginUser(email: String, password: String) {

        CoroutineScope(Dispatchers.Main).launch {
            try {


                withContext(Dispatchers.IO) {
                    authManager.login(email, password)
                }


                val intent = Intent(this@ResetPassActivity, MainActivity::class.java)
                startActivity(intent)
                finish()

            } catch (e: Exception) {

                Toast.makeText(this@ResetPassActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun switchToResetSection() {
        emailSection.visibility = View.GONE
        resetSection.visibility = View.VISIBLE
    }

    private fun setLoadingState(isLoading: Boolean) {
        progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        sendCodeButton.isEnabled = !isLoading
        resetPasswordButton.isEnabled = !isLoading

        // Disable input fields during loading
        emailInput.isEnabled = !isLoading
        codeInput.isEnabled = !isLoading
        newPasswordInput.isEnabled = !isLoading
        confirmPasswordInput.isEnabled = !isLoading
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}