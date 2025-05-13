package com.eiman.servifast.activities

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.eiman.servifast.R
import com.eiman.servifast.api.RetrofitClient
import com.eiman.servifast.api.models.*
import com.eiman.servifast.utils.ValidationUtils.isValidEmail
import com.eiman.servifast.utils.ValidationUtils.isValidPhone
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Intent

class SettingsActivity : AppCompatActivity() {

    private lateinit var inputEmail: EditText
    private lateinit var inputPhone: EditText
    private lateinit var userTypeGroup: RadioGroup
    private lateinit var rbParticular: RadioButton
    private lateinit var rbProfessional: RadioButton
    private lateinit var btnChangeLanguage: Button
    private lateinit var btnSave: Button
    private lateinit var btnBack: ImageButton
    private lateinit var btnChangePassword: Button

    private lateinit var prefs: SharedPreferences
    private var originalEmail = ""
    private var originalPhone = ""
    private var originalUserType = ""
    private var originalLanguage = ""

    private var currentLanguage = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        inputEmail = findViewById(R.id.inputEmail)
        inputPhone = findViewById(R.id.inputPhone)
        userTypeGroup = findViewById(R.id.userTypeGroup)
        rbParticular = findViewById(R.id.rbParticular)
        rbProfessional = findViewById(R.id.rbProfessional)
        btnChangeLanguage = findViewById(R.id.btnChangeLanguage)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)
        btnChangePassword = findViewById(R.id.btnChangePassword)

        prefs = getSharedPreferences("user_session", MODE_PRIVATE)

        originalEmail = ""
        originalPhone = ""
        originalUserType = "particular"
        originalLanguage = prefs.getString("language", "es") ?: "es"
        currentLanguage = originalLanguage

        btnChangeLanguage.text = if (currentLanguage == "es") getString(R.string.espanol) else getString(R.string.ingles)

        btnChangeLanguage.setOnClickListener {
            currentLanguage = if (currentLanguage == "es") "en" else "es"
            btnChangeLanguage.text = if (currentLanguage == "es") getString(R.string.espanol) else getString(R.string.ingles)
        }

        btnSave.setOnClickListener {
            if (!hasChanges()) {
                Toast.makeText(this, "No hay cambios para guardar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newEmail = inputEmail.text.toString().trim()
            val newPhone = inputPhone.text.toString().trim()

            if (newEmail.isNotEmpty() && !isValidEmail(newEmail)) {
                Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPhone.isNotEmpty() && !isValidPhone(newPhone)) {
                Toast.makeText(this, "Teléfono debe tener 9 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(this)
                .setTitle("Confirmar cambios")
                .setMessage("¿Quieres guardar los cambios?")
                .setPositiveButton("Sí") { _, _ -> saveChanges() }
                .setNegativeButton("No", null)
                .show()
        }

        btnBack.setOnClickListener {
            handleBack()
        }

        btnChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }
    }

    override fun onBackPressed() {
        handleBack()
    }

    private fun handleBack() {
        if (hasChanges()) {
            AlertDialog.Builder(this)
                .setTitle("Cambios sin guardar")
                .setMessage("Tienes cambios sin guardar. ¿Deseas salir sin guardar?")
                .setPositiveButton("Sí") { _, _ -> finish() }
                .setNegativeButton("No", null)
                .show()
        } else {
            finish()
        }
    }

    private fun hasChanges(): Boolean {
        val emailChanged = inputEmail.text.toString() != originalEmail
        val phoneChanged = inputPhone.text.toString() != originalPhone
        val userTypeChanged = (rbParticular.isChecked && originalUserType != "particular") ||
                (rbProfessional.isChecked && originalUserType != "profesional")
        val languageChanged = currentLanguage != originalLanguage
        return emailChanged || phoneChanged || userTypeChanged || languageChanged
    }

    private fun saveChanges() {
        val userId = prefs.getString("user_identifier", null) ?: return

        val updatedEmail = if (inputEmail.text.toString() != originalEmail) inputEmail.text.toString() else null
        val updatedPhone = if (inputPhone.text.toString() != originalPhone) inputPhone.text.toString() else null
        val updatedUserType = if (rbParticular.isChecked && originalUserType != "particular") "particular"
        else if (rbProfessional.isChecked && originalUserType != "profesional") "profesional"
        else null
        val updatedLanguageChanged = currentLanguage != originalLanguage

        val request = UpdateUserRequest(
            email = updatedEmail,
            telefono = updatedPhone,
            tipo_usuario = updatedUserType,
            user = userId
        )

        if (updatedEmail != null || updatedPhone != null || updatedUserType != null) {
            RetrofitClient.instance.updateUser(request).enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        if (updatedLanguageChanged) {
                            prefs.edit().putString("language", currentLanguage).apply()
                            originalLanguage = currentLanguage
                        }
                        originalEmail = inputEmail.text.toString()
                        originalPhone = inputPhone.text.toString()
                        originalUserType = if (rbParticular.isChecked) "particular" else "profesional"
                        Toast.makeText(this@SettingsActivity, "Cambios guardados correctamente", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@SettingsActivity, "Error al guardar cambios", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Toast.makeText(this@SettingsActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        } else if (updatedLanguageChanged) {
            prefs.edit().putString("language", currentLanguage).apply()
            originalLanguage = currentLanguage
            Toast.makeText(this, "Idioma actualizado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
