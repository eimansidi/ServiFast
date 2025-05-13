package com.eiman.servifast.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.eiman.servifast.R
import com.eiman.servifast.api.RetrofitClient
import com.eiman.servifast.api.ApiService
import com.eiman.servifast.api.models.LoginRequest
import com.eiman.servifast.api.models.LoginResponse
import com.eiman.servifast.api.models.UserCheckRequest
import com.eiman.servifast.api.models.UserCheckResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var inputUser: EditText
    private lateinit var inputPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        inputUser = findViewById(R.id.inputUser)
        inputPassword = findViewById(R.id.inputPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        btnRegister = findViewById(R.id.btnRegister)

        btnLogin.setOnClickListener {
            val user = inputUser.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            if (user.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(user, password)
            RetrofitClient.instance.login(request).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val body = response.body()!!
                        val nombreCompleto = "${capitalizar(body.nombre ?: "")} ${capitalizar(body.apellidos ?: "")}"

                        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
                        prefs.edit()
                            .putString("user_identifier", user)
                            .putString("user_name", nombreCompleto)
                            .putString("user_avatar", body.avatar)
                            .apply()

                        Toast.makeText(this@LoginActivity, "Bienvenido", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish()
                    }
                    else {
                        Toast.makeText(this@LoginActivity, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        tvForgotPassword.setOnClickListener {
            val user = inputUser.text.toString().trim()
            if (user.isEmpty()) {
                Toast.makeText(this, "Introduce email o teléfono", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = UserCheckRequest(user)
            RetrofitClient.instance.checkUserExists(request).enqueue(object : Callback<UserCheckResponse> {
                override fun onResponse(call: Call<UserCheckResponse>, response: Response<UserCheckResponse>) {
                    if (response.isSuccessful && response.body()?.exists == true) {
                        val intent = Intent(this@LoginActivity, ForgottenPasswordActivity::class.java)
                        intent.putExtra("user_identifier", user)
                        startActivity(intent)
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
