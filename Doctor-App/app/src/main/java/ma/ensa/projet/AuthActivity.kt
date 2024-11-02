package ma.ensa.projet

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthActivity : AppCompatActivity() {

    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)


        authManager = AuthManager(this)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val tvSignup = findViewById<TextView>(R.id.tvSignup)
        val tvResetPass = findViewById<TextView>(R.id.restPass)


        tvSignup.setOnClickListener {
            val intent = Intent(this@AuthActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        tvResetPass.setOnClickListener {
            val intent = Intent(this@AuthActivity, ResetPassActivity::class.java)
            startActivity(intent)
        }

        val btnLogin = findViewById<TextView>(R.id.btnLogin)
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()


            if (!validateEmail(email)) {
                etEmail.error = "Invalid email format"
                return@setOnClickListener
            }


            loginUser(email, password)
        }
    }


    private fun validateEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }


    private fun loginUser(email: String, password: String) {

        CoroutineScope(Dispatchers.Main).launch {
            try {


                withContext(Dispatchers.IO) {
                    authManager.login(email, password)
                }


                val intent = Intent(this@AuthActivity, MainActivity::class.java)
                startActivity(intent)
                finish()

            } catch (e: Exception) {

                Toast.makeText(this@AuthActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
