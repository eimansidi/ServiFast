package com.eiman.servifast.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        aplicarIdioma()

        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val user = prefs.getString("user_identifier", null)

        val intent = if (user != null) {
            Intent(this, HomeActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        finish()
    }

    private fun aplicarIdioma() {
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val language = prefs.getString("language", "es") ?: "es"

        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
