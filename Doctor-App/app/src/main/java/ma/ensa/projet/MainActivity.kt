package ma.ensa.projet

import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment

import androidx.navigation.ui.setupWithNavController
import ma.ensa.projet.databinding.ActivityMainUseBinding

class MainActivity : AppCompatActivity() {
    private lateinit var authManager: AuthManager
    private lateinit var binding: ActivityMainUseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainUseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        authManager = AuthManager(this)
        setupNavigationBasedOnRole()
    }

    private fun setupNavigationBasedOnRole() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main_use) as NavHostFragment
        val navController = navHostFragment.navController
        val navView: BottomNavigationView = binding.navView


        val userRole = authManager.getUserRole()
        Log.d("MainUseActivity", "User role: $userRole")


        val graphResId = if (userRole == 0) {
            R.navigation.mobile_navigation_patient
        } else {
            R.navigation.mobile_navigation_doctor
        }

        val menuResId = if (userRole == 0) {
            R.menu.bottom_nav_menu
        } else {
            R.menu.bottom_nav_menu_doctor
        }

        navView.menu.clear()
        navView.inflateMenu(menuResId)

        val navInflater = navController.navInflater
        val graph = navInflater.inflate(graphResId)
        navController.graph = graph

        navView.setupWithNavController(navController)
    }
}