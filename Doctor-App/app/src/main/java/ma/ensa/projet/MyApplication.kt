package ma.ensa.projet

import android.app.Application
import ma.ensa.projet.api.RetrofitClient
import ma.ensa.projet.api.local.TokenManager

class MyApplication : Application() {
    companion object {
        lateinit var tokenManager: TokenManager
            private set
    }

    override fun onCreate() {
        super.onCreate()
        tokenManager = TokenManager(applicationContext)

        RetrofitClient.initialize(tokenManager)
    }
}
