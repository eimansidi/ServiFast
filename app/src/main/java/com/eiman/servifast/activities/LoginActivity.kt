package com.eiman.servifast.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.eiman.servifast.R
import com.eiman.servifast.api.RetrofitClient
import com.eiman.servifast.api.models.LoginRequest
import com.eiman.servifast.api.models.LoginResponse
import com.eiman.servifast.api.models.UserCheckRequest
import com.eiman.servifast.api.models.UserCheckResponse
import com.eiman.servifast.utils.LocaleHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences

    override fun attachBaseContext(newBase: Context) {
        val shared = newBase.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val lang   = shared.getString("language", "es") ?: "es"
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        prefs = getSharedPreferences("user_session", MODE_PRIVATE)

        findViewById<ImageButton>(R.id.btnEspanol).setOnClickListener {
            prefs.edit().putString("language", "es").apply()
            LocaleHelper.setLocale(this, "es")
            recreate()
        }
        findViewById<ImageButton>(R.id.btnIngles).setOnClickListener {
            prefs.edit().putString("language", "en").apply()
            LocaleHelper.setLocale(this, "en")
            recreate()
        }

        val inputUser       = findViewById<EditText>(R.id.inputUser)
        val inputPassword   = findViewById<EditText>(R.id.inputPassword)
        val btnLogin        = findViewById<Button>(R.id.btnLogin)
        val tvForgot        = findViewById<TextView>(R.id.tvForgotPassword)
        val btnRegister     = findViewById<Button>(R.id.btnRegister)

        btnLogin.setOnClickListener {
            val user     = inputUser.text.toString().trim()
            val password = inputPassword.text.toString().trim()
            if (user.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitClient.instance.login(LoginRequest(user, password))
                .enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            val body = response.body()!!
                            val nombreCompleto = "${capitalizar(body.nombre ?: "")} ${capitalizar(body.apellidos ?: "")}"
                            prefs.edit()
                                .putString("user_identifier", user)
                                .putString("user_name", nombreCompleto)
                                .putString("user_avatar", body.avatar)
                                .apply()
                            Toast.makeText(this@LoginActivity, "Bienvenido", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        }

        tvForgot.setOnClickListener {
            val user = inputUser.text.toString().trim()
            if (user.isEmpty()) {
                Toast.makeText(this, "Introduce email o teléfono", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            RetrofitClient.instance.checkUserExists(UserCheckRequest(user))
                .enqueue(object : Callback<UserCheckResponse> {
                    override fun onResponse(call: Call<UserCheckResponse>, response: Response<UserCheckResponse>) {
                        if (response.isSuccessful && response.body()?.exists == true) {
                            startActivity(Intent(this@LoginActivity, ForgottenPasswordActivity::class.java).apply {
                                putExtra("user_identifier", user)
                            })
                        } else {
                            Toast.makeText(this@LoginActivity, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<UserCheckResponse>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, SigninActivity::class.java))
        }
    }

    private fun capitalizar(texto: String): String {
        return texto.trim().lowercase().split(" ").joinToString(" ") { palabra ->
            palabra.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}
