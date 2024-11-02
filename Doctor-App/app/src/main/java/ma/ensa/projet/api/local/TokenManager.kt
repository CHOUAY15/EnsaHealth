package ma.ensa.projet.api.local

import android.content.Context
import android.content.SharedPreferences

class TokenManager(private val context: Context) {
    companion object {
        private const val PREFS_TOKEN_FILE = "auth_prefs"
        private const val USER_TOKEN = "token"

    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_TOKEN_FILE, Context.MODE_PRIVATE)



    fun getToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }


}