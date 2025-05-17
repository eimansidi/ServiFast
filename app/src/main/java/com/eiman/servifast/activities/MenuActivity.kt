package com.eiman.servifast.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.eiman.servifast.R
import com.eiman.servifast.api.RetrofitClient
import com.eiman.servifast.api.models.UserProfileRequest
import com.eiman.servifast.api.models.UserProfileResponse
import com.eiman.servifast.api.models.UserRatingRequest
import com.eiman.servifast.api.models.UserRatingResponse
import com.eiman.servifast.utils.LocaleHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.roundToInt

class MenuActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var avatarImage: ImageView
    private lateinit var usernameText: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnBack: ImageButton

    override fun attachBaseContext(newBase: Context) {
        val shared = newBase.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val lang   = shared.getString("language", "es") ?: "es"
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu)

        prefs = getSharedPreferences("user_session", MODE_PRIVATE)

        avatarImage   = findViewById(R.id.userAvatar)
        usernameText  = findViewById(R.id.tvUsername)
        btnLogout     = findViewById(R.id.btnLogOut)
        btnBack       = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }
        btnLogout.setOnClickListener { showLogoutConfirmation() }
        setNavigationListeners()

        loadUserInfo()
        loadUserRating()
    }

    override fun onResume() {
        super.onResume()
        loadUserInfo()
        loadUserRating()
    }

    private fun loadUserInfo() {
        val userId = prefs.getString("user_identifier", null) ?: return
        RetrofitClient.instance.getUserProfile(UserProfileRequest(userId))
            .enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(
                    call: Call<UserProfileResponse>,
                    response: Response<UserProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.success == true) {
                            // Avatar
                            if (!body.avatar.isNullOrEmpty()) {
                                val bytes = Base64.decode(body.avatar, Base64.NO_WRAP)
                                avatarImage.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                            } else {
                                avatarImage.setImageResource(R.drawable.generic_avatar)
                            }
                            // Username
                            usernameText.text = "${body.nombre.capitalize()} ${body.apellidos.capitalize()}"
                        } else {
                            Toast.makeText(this@MenuActivity, "Error servidor: ${body ?: "Desconocido"}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@MenuActivity, "Error HTTP: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    Toast.makeText(this@MenuActivity, "Error red: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                prefs.edit().clear().apply()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun setNavigationListeners() {
        findViewById<TextView>(R.id.tvProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<TextView>(R.id.tvFavorites).setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }
        findViewById<TextView>(R.id.tvMyServices).setOnClickListener {
            startActivity(Intent(this, MyServicesActivity::class.java))
        }
        findViewById<TextView>(R.id.tvBought).setOnClickListener {
            startActivity(Intent(this, BoughtActivity::class.java))
        }
        findViewById<TextView>(R.id.tvSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun loadUserRating() {
        val userId = prefs.getString("user_identifier", null) ?: return
        RetrofitClient.instance
            .getUserRating(UserRatingRequest(userId))
            .enqueue(object : Callback<UserRatingResponse> {
                override fun onResponse(
                    call: Call<UserRatingResponse>,
                    response: Response<UserRatingResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        renderStars(response.body()!!.rating)
                    } else {
                        Toast.makeText(
                            this@MenuActivity,
                            "No se pudo cargar rating",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                override fun onFailure(call: Call<UserRatingResponse>, t: Throwable) {
                    Toast.makeText(
                        this@MenuActivity,
                        "Error al cargar rating: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun renderStars(rating: Float) {
        val stars = listOf<ImageView>(
            findViewById(R.id.star1),
            findViewById(R.id.star2),
            findViewById(R.id.star3),
            findViewById(R.id.star4),
            findViewById(R.id.star5)
        )
        val filled = rating.roundToInt()
        for ((index, star) in stars.withIndex()) {
            val colorRes = if (index < filled) R.color.star_active else R.color.star_inactive
            star.setColorFilter(
                ContextCompat.getColor(this, colorRes),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
    }
}
