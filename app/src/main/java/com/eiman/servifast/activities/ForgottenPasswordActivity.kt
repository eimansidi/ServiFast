package com.eiman.servifast.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.eiman.servifast.R
import com.eiman.servifast.api.RetrofitClient
import com.eiman.servifast.api.models.GenericResponse
import com.eiman.servifast.api.models.ResetPasswordRequest
import com.eiman.servifast.utils.LocaleHelper
import com.eiman.servifast.utils.ValidationUtils.isValidPassword
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgottenPasswordActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun attachBaseContext(newBase: Context) {
        val shared = newBase.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val lang   = shared.getString("language", "es") ?: "es"
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgotten_password)

        prefs = getSharedPreferences("user_session", MODE_PRIVATE)

        // --- botones de idioma ---
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

        val inputNew    = findViewById<EditText>(R.id.inputNewPassword)
        val inputRepeat = findViewById<EditText>(R.id.inputRepeatNewPassword)
        val btnSave     = findViewById<Button>(R.id.btnSave)
        val userId      = intent.getStringExtra("user_identifier") ?: ""

        btnSave.setOnClickListener {
            val newPassword    = inputNew.text.toString().trim()
            val repeatPassword = inputRepeat.text.toString().trim()
            if (newPassword.isEmpty() || repeatPassword.isEmpty()) {
                Toast.makeText(this, "Introduce ambas contraseñas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPassword != repeatPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isValidPassword(newPassword)) {
                Toast.makeText(
                    this,
                    "Contraseña débil: mínimo 8 caracteres, un número y un símbolo",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            RetrofitClient.instance.resetPassword(ResetPasswordRequest(user = userId, new_password = newPassword))
                .enqueue(object : Callback<GenericResponse> {
                    override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(this@ForgottenPasswordActivity, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@ForgottenPasswordActivity, LoginActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@ForgottenPasswordActivity, "Error al actualizar contraseña", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                        Toast.makeText(this@ForgottenPasswordActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        }
    }
}
