package com.eiman.servifast.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.eiman.servifast.R
import com.eiman.servifast.api.RetrofitClient
import com.eiman.servifast.api.models.ChangePasswordRequest
import com.eiman.servifast.api.models.GenericResponse
import com.eiman.servifast.utils.ValidationUtils.isValidPassword
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var inputNewPassword: EditText
    private lateinit var inputRepeatPassword: EditText
    private lateinit var btnSave: Button
    private lateinit var btnBack: ImageButton

    private lateinit var userIdentifier: String
    private var currentPassword: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_password)

        inputNewPassword = findViewById(R.id.inputNewPassword)
        inputRepeatPassword = findViewById(R.id.inputRepeatNewPassword)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)

        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        userIdentifier = prefs.getString("user_identifier", null) ?: ""
        currentPassword = prefs.getString("user_password", null)

        btnBack.setOnClickListener { onBackPressed() }

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

            if (newPassword == currentPassword) {
                Toast.makeText(this, "La nueva contraseña no puede ser igual a la actual", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = ChangePasswordRequest(user = userIdentifier, new_password = newPassword)

            RetrofitClient.instance.changePassword(request).enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        AlertDialog.Builder(this@ChangePasswordActivity)
                            .setTitle("Éxito")
                            .setMessage("Tu contraseña ha sido actualizada correctamente.")
                            .setPositiveButton("OK") { _, _ ->
                                startActivity(Intent(this@ChangePasswordActivity, SettingsActivity::class.java))
                                finish()
                            }
                            .setCancelable(false)
                            .show()
                    } else {
                        Toast.makeText(this@ChangePasswordActivity, "Error al actualizar contraseña", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Toast.makeText(this@ChangePasswordActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}
