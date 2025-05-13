package com.eiman.servifast.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.eiman.servifast.R
import com.eiman.servifast.api.RetrofitClient
import com.eiman.servifast.api.models.ResetPasswordRequest
import com.eiman.servifast.api.models.GenericResponse
import com.eiman.servifast.utils.ValidationUtils.isValidPassword
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgottenPasswordActivity : AppCompatActivity() {
    private lateinit var inputNewPassword: EditText
    private lateinit var inputRepeatPassword: EditText
    private lateinit var btnSave: Button
    private lateinit var userIdentifier: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgotten_password)

        inputNewPassword = findViewById(R.id.inputNewPassword)
        inputRepeatPassword = findViewById(R.id.inputRepeatNewPassword)
        btnSave = findViewById(R.id.btnSave)

        userIdentifier = intent.getStringExtra("user_identifier") ?: ""

        btnSave.setOnClickListener {
            val newPassword = inputNewPassword.text.toString().trim()
            val repeatPassword = inputRepeatPassword.text.toString().trim()

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

            val request = ResetPasswordRequest(user = userIdentifier, new_password = newPassword)
            RetrofitClient.instance.resetPassword(request).enqueue(object : Callback<GenericResponse> {
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
