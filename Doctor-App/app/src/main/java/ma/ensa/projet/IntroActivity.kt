package ma.ensa.projet

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class IntroActivity : AppCompatActivity() {
    private val splashScreenDuration: Long = 3000 // Duration in milliseconds (3 seconds)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_intro)

        Log.d("IntroActivity", "onCreate called")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Using Handler to delay the transition to AuthActivity
        Handler().postDelayed({
            Log.d("IntroActivity", "Navigating to AuthActivity")
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish() // Close IntroActivity
        }, splashScreenDuration)
    }
}
